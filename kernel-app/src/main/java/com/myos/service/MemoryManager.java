package com.myos.service;

import com.myos.model.PageTable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemoryManager {

    private static final int TOTAL_MEMORY_SIZE =1000;
    private static final int PAGE_SIZE = 10; // حجم الصفحة والـ Frame
    private static final int TOTAL_FRAMES = TOTAL_MEMORY_SIZE / PAGE_SIZE;


    private final boolean[] frames = new boolean[TOTAL_FRAMES];


    public synchronized PageTable allocate(int totalSize) {

        int pagesNeeded = (int) Math.ceil((double) totalSize / PAGE_SIZE);

        List<Integer> freeFrames = findFreeFrames(pagesNeeded);

        if (freeFrames.size() == pagesNeeded) {
            PageTable pageTable = new PageTable();

            for (int i = 0; i < pagesNeeded; i++) {
                int frameNumber = freeFrames.get(i);

                frames[frameNumber] = true;

                pageTable.addEntry(i, frameNumber);

                System.out.println("📄 Mapped Page " + i + " -> Frame " + frameNumber);
            }
            return pageTable;
        }

        System.out.println("❌ Not Enough Frames! Needed: " + pagesNeeded + ", Available: " + freeFrames.size());
        return null;
    }


    public synchronized void deallocate(PageTable pageTable) {
        for (int i = 0; i < pageTable.getPageCount(); i++) {
            int frameNumber = pageTable.getFrameNumber(i);
            if (frameNumber != -1) {
                frames[frameNumber] = false; // فضي العلبة
            }
        }
        System.out.println("♻️ Released Frames for Process");
    }


    private List<Integer> findFreeFrames(int countNeeded) {
        List<Integer> foundFrames = new ArrayList<>();

        for (int i = 0; i < TOTAL_FRAMES; i++) {
            if (!frames[i]) {
                foundFrames.add(i);

                if (foundFrames.size() == countNeeded) {
                    break;
                }
            }
        }
        return foundFrames;
    }
}