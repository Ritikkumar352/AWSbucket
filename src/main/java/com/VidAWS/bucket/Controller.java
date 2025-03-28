package com.VidAWS.bucket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class Controller {

    @Autowired
    private S3Service service;
    @GetMapping("/")
    public String index() {
        return "Hello World";
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String,String>> upload(@RequestParam("file")MultipartFile file) {
        return service.upload(file);

    }
}
