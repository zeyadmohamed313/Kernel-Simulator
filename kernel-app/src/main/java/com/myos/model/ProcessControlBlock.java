package com.myos.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessControlBlock {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private int pid;
    private String name;
    private int totalInstructions;
    private int programCounter;
    private ProcessState state;
    private final List<Integer> allocatedResources = new ArrayList<>();
    private Integer waitingForResourceId = null;
    private PageTable pageTable;


    public ProcessControlBlock(String name, int totalInstructions) {
        this.pid = ID_GENERATOR.getAndIncrement();
        this.name = name;
        this.totalInstructions = totalInstructions;
        this.programCounter = 0;
        this.state = ProcessState.NEW;
    }

    public void executeStep() {
        if (programCounter < totalInstructions) programCounter++;
    }

    public boolean isFinished() {
        return programCounter >= totalInstructions;
    }

    public int getPid() { return pid; }
    public String getName() { return name; }
    public ProcessState getState() { return state; }
    public void setState(ProcessState state) { this.state = state; }
    public int getProgramCounter() { return programCounter; }

    public PageTable getPageTable() { return pageTable; }
    public void setPageTable(PageTable pageTable) { this.pageTable = pageTable; }
    public List<Integer> getAllocatedResources() {return allocatedResources;}
    public Integer getWaitingForResourceId() {return waitingForResourceId;}
    public void setWaitingForResourceId(Integer resourceId) {this.waitingForResourceId = resourceId;}
    @Override
    public String toString() {
        return "Process [ID=" + pid + ", Name=" + name + ", State=" + state + "]";
    }
}