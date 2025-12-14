package com.myos.model.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualDirectory extends FSNode {

    // الخزنة: بنشيل فيها العيال.
    // Key: اسم الملف (String)
    // Value: الملف نفسه (FSNode)
    private final Map<String, FSNode> children = new ConcurrentHashMap<>();

    public VirtualDirectory(String name, int ownerPid) {
        super(name, ownerPid);
    }

    // إضافة ملف جديد جوه الفولدر
    public void addChild(FSNode node) {
        children.put(node.getName(), node);
    }

    // هاتلي ملف بالاسم
    public FSNode getChild(String name) {
        return children.get(name);
    }

    // امسح ملف
    public void removeChild(String name) {
        children.remove(name);
    }

    // هاتلي قائمة بكل أسماء الملفات اللي هنا (عشان أمر ls)
    public List<String> listChildren() {
        return new ArrayList<>(children.keySet());
    }

    @Override
    public String getType() {
        return "DIR";
    }
}