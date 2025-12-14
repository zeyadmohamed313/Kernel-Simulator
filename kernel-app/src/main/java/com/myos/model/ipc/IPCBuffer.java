package com.myos.model.ipc;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class IPCBuffer {

    private final int id;
    private final Queue<String> buffer;

    // --- أدوات التحكم (Semaphores) ---
    private final Semaphore mutex;
    private final Semaphore emptySlots;
    private final Semaphore fullSlots;

    public IPCBuffer(int id, int capacity) {
        this.id = id;
        this.buffer = new LinkedList<>();


        this.mutex = new Semaphore(1);

        this.emptySlots = new Semaphore(capacity);


        this.fullSlots = new Semaphore(0);
    }


    public void write(String data) throws InterruptedException {

        emptySlots.acquire();


        mutex.acquire();

        try {
            buffer.add(data);
            System.out.println("📥 IPC [" + id + "] WRITE: " + data);
        } finally {
            mutex.release();
        }


        fullSlots.release();
    }

    public String read() throws InterruptedException {

        fullSlots.acquire();

        mutex.acquire();

        String data = null;
        try {
            // خطوة ج: اسحب الداتا
            data = buffer.poll();
            System.out.println("📤 IPC [" + id + "] READ: " + data);
        } finally {
            // خطوة د: سيب القفل
            mutex.release();
        }

        emptySlots.release();

        return data;
    }

    public int getId() { return id; }
}