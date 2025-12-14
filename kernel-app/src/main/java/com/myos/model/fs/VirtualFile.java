package com.myos.model.fs;

public class VirtualFile extends FSNode {

    // المحتوى: بنستخدم StringBuilder عشان التعديل عليه أسرع من String العادية
    private StringBuilder content;

    public VirtualFile(String name, int ownerPid) {
        super(name, ownerPid); // نبعت الاسم والمالك للجد (FSNode)
        this.content = new StringBuilder(); // بنبدأ بملف فاضي
    }

    // ==========================================
    // دالة الكتابة (محمية بـ Write Lock) ⛔
    // ==========================================
    public void writeContent(String data) {
        writeLock(); // 1. اقفل الباب (ممنوع حد تاني يدخل)
        try {
            // محاكاة إن الكتابة بتاخد وقت (عشان نحس بالـ Threading بعدين)
            Thread.sleep(50);
            content.append(data); // زود الكلام الجديد على القديم
            content.append("\n"); // انزل سطر

            System.out.println("📝 PID " + Thread.currentThread().getId() + " wrote to: " + name);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            writeUnlock(); // 2. افتح الباب (مهم جداً نعملها في finally عشان لو حصل error الباب ميفضلش مقفول للأبد)
        }
    }

    // ==========================================
    // دالة القراءة (محمية بـ Read Lock) 👀
    // ==========================================
    public String readContent() {
        readLock(); // 1. استأذن في القراءة (عادي لو ناس تانية بتقرأ)
        try {
            return content.toString();
        } finally {
            readUnlock(); // 2. خلصت قراءة
        }
    }

    @Override
    public String getType() {
        return "FILE";
    }
}