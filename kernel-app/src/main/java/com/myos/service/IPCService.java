package com.myos.service;

import com.myos.model.ipc.IPCBuffer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IPCService {

    private final Map<Integer, IPCBuffer> buffers = new ConcurrentHashMap<>();

    private int bufferIdCounter = 1;

    public int createBuffer(int capacity) {
        int id = bufferIdCounter++;
        IPCBuffer buffer = new IPCBuffer(id, capacity);
        buffers.put(id, buffer);

        System.out.println("✅ IPC Created: Buffer " + id + " with capacity " + capacity);
        return id;
    }

    public IPCBuffer getBuffer(int id) {
        return buffers.get(id);
    }
}