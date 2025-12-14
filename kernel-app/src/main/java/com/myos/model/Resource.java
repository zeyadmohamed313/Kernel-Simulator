package com.myos.model;

public class Resource {
    private final int id;
    private final String name;

    private int ownerPid = -1;

    public Resource(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isLocked() {
        return ownerPid != -1;
    }

    public void lock(int pid) {
        this.ownerPid = pid;
    }

    public void unlock() {
        this.ownerPid = -1;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getOwnerPid() { return ownerPid; }
}