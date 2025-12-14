package com.myos.model.fs;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class FSNode {

    // 1. البيانات الأساسية لأي حاجة في السيستم
    protected String name;      // اسم الملف أو الفولدر
    protected int ownerPid;     // رقم العملية اللي خلقته (عشان الصلاحيات)

    // 2. القفل الذكي (إشارة المرور)
    // بنستخدم final عشان القفل ده اتخلق مرة واحدة ومينفعش يتغير
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // الكونستركتور: أي حد هيورثني لازم يديني الاسم والمالك
    public FSNode(String name, int ownerPid) {
        this.name = name;
        this.ownerPid = ownerPid;
    }

    // --- دوال مساعدة عشان منكتبش كود القفل الطويل في كل حتة ---

    // دالة: "أنا عايز أقرأ" (بتسمح بالزحمة)
    public void readLock() {
        lock.readLock().lock();
    }

    // دالة: "أنا خلصت قراءة"
    public void readUnlock() {
        lock.readLock().unlock();
    }

    // دالة: "أنا عايز أكتب" (ممنوع الاقتراب أو التصوير)
    public void writeLock() {
        lock.writeLock().lock();
    }

    // دالة: "أنا خلصت كتابة"
    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    // Getters
    public String getName() { return name; }
    public int getOwnerPid() { return ownerPid; }

    // دالة كل ابن هينفذها بطريقته (يقول هو ملف ولا فولدر)
    public abstract String getType();
}