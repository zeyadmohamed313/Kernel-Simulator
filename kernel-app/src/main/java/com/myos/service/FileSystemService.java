package com.myos.service;

import com.myos.model.fs.FSNode;
import com.myos.model.fs.VirtualDirectory;
import com.myos.model.fs.VirtualFile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileSystemService {

    private final VirtualDirectory root;

    public FileSystemService() {
        // بنخلق الروت، والمالك هو السيستم (PID 0)
        this.root = new VirtualDirectory("/", 0);
        System.out.println("📂 File System Initialized. Root is mounted.");
    }


    public String createDirectory(String parentPath, String dirName, int ownerPid) {
        VirtualDirectory parent = resolveDirectory(parentPath);

        if (parent == null) return "Error: Parent directory not found!";

        if (parent.getChild(dirName) != null) {
            return "Error: Directory already exists!";
        }

        VirtualDirectory newDir = new VirtualDirectory(dirName, ownerPid);
        parent.addChild(newDir);

        return "✅ Directory created: " + parentPath + (parentPath.equals("/") ? "" : "/") + dirName;
    }


    public String createFile(String parentPath, String fileName, int ownerPid) {
        VirtualDirectory parent = resolveDirectory(parentPath);

        if (parent == null) return "Error: Parent directory not found!";

        if (parent.getChild(fileName) != null) {
            return "Error: File already exists!";
        }

        VirtualFile newFile = new VirtualFile(fileName, ownerPid);
        parent.addChild(newFile);

        return "✅ File created: " + fileName;
    }


    public List<String> listDirectory(String path) {
        VirtualDirectory dir = resolveDirectory(path);
        if (dir == null) return List.of("Error: Directory not found");

        return dir.listChildren();
    }

    public String writeToFile(String parentPath, String fileName, String content) {
        VirtualDirectory parent = resolveDirectory(parentPath);
        if (parent == null) return "Error: Directory not found";

        FSNode node = parent.getChild(fileName);

        if (node instanceof VirtualFile) {
            VirtualFile file = (VirtualFile) node;
            file.writeContent(content);
            return "✅ Wrote to file: " + fileName;
        } else {
            return "Error: File not found or is a directory!";
        }
    }


    public String readFile(String parentPath, String fileName) {
        VirtualDirectory parent = resolveDirectory(parentPath);
        if (parent == null) return "Error: Directory not found";

        FSNode node = parent.getChild(fileName);

        if (node instanceof VirtualFile) {
            return ((VirtualFile) node).readContent();
        }
        return "Error: File not found!";
    }


    private VirtualDirectory resolveDirectory(String path) {
        if (path.equals("/")) return root;


        FSNode node = root.getChild(path);
        if (node instanceof VirtualDirectory) {
            return (VirtualDirectory) node;
        }
        return null;
    }
}