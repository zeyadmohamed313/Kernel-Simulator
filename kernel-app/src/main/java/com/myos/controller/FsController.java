package com.myos.controller;

import com.myos.DTO.FileWriteRequest;
import com.myos.service.FileSystemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fs")
public class FsController {

    private final FileSystemService fileSystemService;

    public FsController(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }


    @PostMapping("/mkdir")
    public String createDirectory(@RequestParam String parent,
                                  @RequestParam String name,
                                  @RequestParam int owner) {
        return fileSystemService.createDirectory(parent, name, owner);
    }


    @PostMapping("/touch")
    public String createFile(@RequestParam String parent,
                             @RequestParam String name,
                             @RequestParam int owner) {
        return fileSystemService.createFile(parent, name, owner);
    }


    @GetMapping("/ls")
    public List<String> listDirectory(@RequestParam String path) {
        return fileSystemService.listDirectory(path);
    }


    @PostMapping("/write")
    public String writeToFile(@RequestBody FileWriteRequest request) {
        return fileSystemService.writeToFile(request.parentPath, request.fileName, request.content);
    }

    @GetMapping("/cat")
    public String readFile(@RequestParam String parent,
                           @RequestParam String name) {
        return fileSystemService.readFile(parent, name);
    }
}