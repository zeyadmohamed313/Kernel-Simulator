package com.myos.service;

import com.myos.model.Resource;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResourceManager {

    private final Map<Integer, Resource> resources = new ConcurrentHashMap<>();

    public ResourceManager() {
        resources.put(1, new Resource(1, "Printer 🖨️"));
        resources.put(2, new Resource(2, "File System 📂"));
        resources.put(3, new Resource(3, "Network Card 🌐"));
    }

    public synchronized boolean tryAcquire(int resourceId, int pid) {
        Resource res = resources.get(resourceId);

        if (res == null) return false;

        if (!res.isLocked()) {
            res.lock(pid);
            return true;
        } else {
            return false;
        }
    }

    public synchronized void release(int resourceId, int pid) {
        Resource res = resources.get(resourceId);

        if (res != null && res.getOwnerPid() == pid) {
            res.unlock();
        }
    }
    public Resource getResource(int id) {
        return resources.get(id);
    }
}