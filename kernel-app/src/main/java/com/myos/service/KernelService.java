package com.myos.service;

import com.myos.model.PageTable;
import com.myos.model.ProcessControlBlock;
import com.myos.model.ProcessState;
import com.myos.model.ipc.IPCBuffer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class KernelService {

    private final MemoryManager memoryManager;
    private final ResourceManager resourceManager;
    private final IPCService ipcService;
    private static final int TIME_QUANTUM = 3;
    private static final int INSTRUCTION_DURATION_MS = 1000;

    private final Queue<ProcessControlBlock> readyQueue = new ConcurrentLinkedQueue<>();

    private final Queue<ProcessControlBlock> waitingQueue = new ConcurrentLinkedQueue<>();

    private final Map<Integer, ProcessControlBlock> processTable = new ConcurrentHashMap<>();

    public KernelService(MemoryManager memoryManager, ResourceManager resourceManager,
                         IPCService ipcService) {
        this.memoryManager = memoryManager;
        this.resourceManager = resourceManager;
        this.ipcService = ipcService;
    }

    public int submitProcess(String name, int totalInstructions, int memorySize) {
        ProcessControlBlock newProcess = new ProcessControlBlock(name, totalInstructions);
        PageTable pageTable = memoryManager.allocate(memorySize);
        if (pageTable != null) {
            newProcess.setPageTable(pageTable);
            newProcess.setState(ProcessState.READY);
            processTable.put(newProcess.getPid(), newProcess);
            readyQueue.add(newProcess);
            return newProcess.getPid();
        }
        return -1;
    }


    public String requestResource(int pid, int resourceId) {
        ProcessControlBlock pcb = processTable.get(pid);
        if (pcb == null || pcb.getState() == ProcessState.TERMINATED) return "Process not found";

        // حاول تحجز المورد من المدير
        boolean success = resourceManager.tryAcquire(resourceId, pid);

        if (success) {
            pcb.getAllocatedResources().add(resourceId);
            return "✅ Granted: Process " + pid + " acquired Resource " + resourceId;
        } else {

            pcb.setState(ProcessState.WAITING);
            pcb.setWaitingForResourceId(resourceId);

            readyQueue.remove(pcb);
            if (!waitingQueue.contains(pcb)) {
                waitingQueue.add(pcb);
            }

            return "⏳ Blocked: Process " + pid + " is WAITING for Resource " + resourceId;
        }
    }


    public String releaseResource(int pid, int resourceId) {
        ProcessControlBlock pcb = processTable.get(pid);
        if (pcb == null) return "Process not found";

        resourceManager.release(resourceId, pid);
        pcb.getAllocatedResources().remove(Integer.valueOf(resourceId));

        wakeUpWaitingProcesses(resourceId);

        return "🔓 Released: Process " + pid + " freed Resource " + resourceId;
    }

    private void wakeUpWaitingProcesses(int freedResourceId) {
        for (ProcessControlBlock waitingProcess : waitingQueue) {
            if (waitingProcess.getWaitingForResourceId() != null &&
                    waitingProcess.getWaitingForResourceId() == freedResourceId) {

                if (resourceManager.tryAcquire(freedResourceId, waitingProcess.getPid())) {
                    waitingProcess.setWaitingForResourceId(null);
                    waitingProcess.getAllocatedResources().add(freedResourceId);

                    waitingProcess.setState(ProcessState.READY);
                    waitingQueue.remove(waitingProcess);
                    readyQueue.add(waitingProcess);

                    System.out.println("🔔 WAKE UP: Process " + waitingProcess.getPid() + " got Resource " + freedResourceId);
                    break;
                }
            }
        }
    }


    @PostConstruct
    public void startScheduler() {
        new Thread(() -> {
            while (true) {
                try {
                    if (readyQueue.isEmpty()) {
                        Thread.sleep(1000);
                        continue;
                    }

                    ProcessControlBlock currentProcess = readyQueue.poll();

                    if (currentProcess.getState() == ProcessState.WAITING) continue;
                    if (currentProcess.getState() == ProcessState.TERMINATED) continue;

                    currentProcess.setState(ProcessState.RUNNING);
                    System.out.println("▶️ Executing PID: " + currentProcess.getPid());

                    int instructionsExecuted = 0;
                    while (instructionsExecuted < TIME_QUANTUM && !currentProcess.isFinished()) {

                        if (currentProcess.getState() == ProcessState.WAITING) {
                            System.out.println("⏸️ Process " + currentProcess.getPid() + " BLOCKED immediately!");
                            break;
                        }

                        currentProcess.executeStep();
                        instructionsExecuted++;
                        Thread.sleep(INSTRUCTION_DURATION_MS);
                        System.out.print(".");
                    }
                    System.out.println();

                    if (currentProcess.getState() == ProcessState.WAITING) {
                    }
                    else if (currentProcess.isFinished()) {
                        currentProcess.setState(ProcessState.TERMINATED);
                        memoryManager.deallocate(currentProcess.getPageTable());

                        for (int resId : new java.util.ArrayList<>(currentProcess.getAllocatedResources())) {
                            releaseResource(currentProcess.getPid(), resId);
                        }

                        System.out.println("✅ Finished: " + currentProcess.getName());
                    }
                    else {
                        currentProcess.setState(ProcessState.READY);
                        readyQueue.add(currentProcess);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    public Map<Integer, ProcessControlBlock> getProcessTable() { return processTable; }



    public String writeToIPC(int pid, int bufferId, String data) {
        ProcessControlBlock pcb = processTable.get(pid);
        if (pcb == null) return "Error: Process not found";

        IPCBuffer buffer = ipcService.getBuffer(bufferId);
        if (buffer == null) return "Error: Buffer not found";

        try {

            buffer.write(data);
            return "✅ Process " + pid + " wrote to Buffer " + bufferId;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "❌ Interrupted while writing";
        }
    }

    public String readFromIPC(int pid, int bufferId) {
        ProcessControlBlock pcb = processTable.get(pid);
        if (pcb == null) return "Error: Process not found";

        IPCBuffer buffer = ipcService.getBuffer(bufferId);
        if (buffer == null) return "Error: Buffer not found";

        try {
            String data = buffer.read();
            return "✅ Process " + pid + " read: " + data;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "❌ Interrupted while reading";
        }
    }


    // ================= 4. إنهاء عملية إجبارياً (Kill) =================
    public void terminateProcess(int pid) {
        ProcessControlBlock pcb = processTable.get(pid);
        if (pcb == null) return;

        // 1. غير حالتها
        pcb.setState(ProcessState.TERMINATED);

        // 2. شيلها من الطوابير
        readyQueue.remove(pcb);
        waitingQueue.remove(pcb);

        // 3. حرر الذاكرة
        if (pcb.getPageTable() != null) {
            memoryManager.deallocate(pcb.getPageTable());
        }

        // 4. حرر الموارد اللي كانت ماسكاها (عشان غيرها ياخدها)
        // بنسخ القائمة عشان ميديناش Error واحنا بنلف عليها
        for (int resId : new java.util.ArrayList<>(pcb.getAllocatedResources())) {
            releaseResource(pid, resId);
        }

        System.out.println("💀 Process " + pid + " Terminated by System (Deadlock Resolution).");
    }

}