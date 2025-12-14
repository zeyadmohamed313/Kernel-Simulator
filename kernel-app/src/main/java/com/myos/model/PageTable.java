package com.myos.model;

import java.util.HashMap;
import java.util.Map;

public class PageTable {


    private final Map<Integer, Integer> pageFrameMap = new HashMap<>();


    public void addEntry(int pageNumber, int frameNumber) {
        pageFrameMap.put(pageNumber, frameNumber);
    }


    public int getFrameNumber(int pageNumber) {
        return pageFrameMap.getOrDefault(pageNumber, -1);
    }


    public int getPageCount() {
        return pageFrameMap.size();
    }

    @Override
    public String toString() {
        return pageFrameMap.toString();
    }
}