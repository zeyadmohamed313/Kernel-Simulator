package com.myos.controller;

import com.myos.model.ProcessControlBlock;
import com.myos.service.IPCService;
import com.myos.service.KernelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/os")
public class OsController {

    private final KernelService kernelService;
    private final IPCService ipcService;

    public OsController(KernelService kernelService,IPCService ipcService) {
        this.kernelService = kernelService;
        this.ipcService = ipcService;
    }

    // ==========================================
    // 1. إنشاء عملية جديدة (Simulate: ./program.exe)
    // ==========================================
    @PostMapping("/submit")
    public ResponseEntity<String> submitProcess(@RequestBody ProcessRequest request) {

        // بننادي على الـ Long-Term Scheduler (Kernel)
        int pid = kernelService.submitProcess(
                request.name(),
                request.totalInstructions(),
                request.memoryRequired()
        );

        if (pid != -1) {
            return ResponseEntity.ok("✅ Process Created Successfully! PID: " + pid);
        } else {
            return ResponseEntity.status(400).body("⛔ Allocation Failed: Not enough contiguous memory.");
        }
    }



    @PostMapping("/submit-batch")
    public ResponseEntity<String> submitBatch(@RequestBody java.util.List<ProcessRequest> requests) {
        StringBuilder result = new StringBuilder();

        for (ProcessRequest req : requests) {
            int pid = kernelService.submitProcess(req.name(), req.totalInstructions(), req.memoryRequired());

            if (pid != -1) {
                result.append("✅ Created: ").append(req.name()).append(" (PID: ").append(pid).append(")\n");
            } else {
                result.append("⛔ Failed: ").append(req.name()).append("\n");
            }
        }

        return ResponseEntity.ok(result.toString());
    }


    @GetMapping("/status")
    public Collection<ProcessControlBlock> getSystemStatus() {
        return kernelService.getProcessTable().values();
    }

    @PostMapping("/process/{pid}/acquire/{resourceId}")
    public ResponseEntity<String> acquireResource(@PathVariable int pid, @PathVariable int resourceId) {
        String result = kernelService.requestResource(pid, resourceId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/process/{pid}/release/{resourceId}")
    public ResponseEntity<String> releaseResource(@PathVariable int pid, @PathVariable int resourceId) {
        String result = kernelService.releaseResource(pid, resourceId);
        return ResponseEntity.ok(result);
    }

    // ================= 🆕 IPC Endpoints =================


    @PostMapping("/ipc/create")
    public ResponseEntity<String> createBuffer(@RequestParam int capacity) {
        int id = ipcService.createBuffer(capacity);
        return ResponseEntity.ok("Buffer Created with ID: " + id);
    }

    @PostMapping("/ipc/write")
    public ResponseEntity<String> writeToBuffer(@RequestParam int pid,
                                                @RequestParam int bufferId,
                                                @RequestParam String data) {
        String result = kernelService.writeToIPC(pid, bufferId, data);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/ipc/read")
    public ResponseEntity<String> readFromBuffer(@RequestParam int pid,
                                                 @RequestParam int bufferId) {
        String result = kernelService.readFromIPC(pid, bufferId);
        return ResponseEntity.ok(result);
    }


    public record ProcessRequest(String name, int totalInstructions, int memoryRequired) {}
}