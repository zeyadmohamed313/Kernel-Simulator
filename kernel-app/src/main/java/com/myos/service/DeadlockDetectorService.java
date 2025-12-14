package com.myos.service;

import com.myos.model.ProcessControlBlock;
import com.myos.model.ProcessState;
import com.myos.model.Resource;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class DeadlockDetectorService {

    private final KernelService kernelService;
    private final ResourceManager resourceManager;

    public DeadlockDetectorService(KernelService kernelService, ResourceManager resourceManager) {
        this.kernelService = kernelService;
        this.resourceManager = resourceManager;
    }

    @PostConstruct
    public void startDetection() {
        new Thread(() -> {
            System.out.println("🕵️‍♂️ Deadlock Detector Started...");

            while (true) {
                try {
                    Thread.sleep(5000);
                    detectAndResolveDeadlocks();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private void detectAndResolveDeadlocks() {
        Map<Integer, ProcessControlBlock> processes = kernelService.getProcessTable();

        for (ProcessControlBlock p : processes.values()) {

            if (p.getState() == ProcessState.WAITING) {

                if (isDeadlocked(p, processes)) {
                    System.out.println("🚨 DEADLOCK DETECTED involving PID: " + p.getPid());
                    resolveDeadlock(p);
                }
            }
        }
    }

    private boolean isDeadlocked(ProcessControlBlock startProcess, Map<Integer, ProcessControlBlock> allProcesses) {
        Set<Integer> visitedPids = new HashSet<>();
        ProcessControlBlock current = startProcess;

        while (current != null && current.getState() == ProcessState.WAITING) {

            Integer waitingForResId = current.getWaitingForResourceId();
            if (waitingForResId == null) return false;

            Resource res = resourceManager.getResource(waitingForResId);
            int ownerPid = res.getOwnerPid();

            if (ownerPid == -1) return false;

            if (ownerPid == startProcess.getPid()) {
                return true;
            }

            if (visitedPids.contains(ownerPid)) {
                return true;
            }
            visitedPids.add(current.getPid());

            current = allProcesses.get(ownerPid);
        }

    }

    private void resolveDeadlock(ProcessControlBlock victim) {
        System.out.println("⚔️ Killing Process " + victim.getPid() + " to resolve Deadlock.");

        kernelService.terminateProcess(victim.getPid());
    }
}