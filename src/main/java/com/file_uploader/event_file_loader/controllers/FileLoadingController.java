package com.file_uploader.event_file_loader.controllers;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.file_uploader.event_file_loader.service.FileUploaderService;


@RestController
@RequestMapping("/app")
public class FileLoadingController {

    @Autowired
    private  FileUploaderService fileService;

    private final  Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();
    

    @GetMapping(value = "/Progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter eventEmitter() throws IOException{
        SseEmitter emitter = new SseEmitter(-1L);
        UUID guid = UUID.randomUUID();
        System.out.println(guid.toString());
        sseEmitterMap.put(guid.toString(),emitter);
        emitter.send(SseEmitter.event().name("uuid").data(guid));
        emitter.onCompletion(()->sseEmitterMap.remove(guid.toString()));
        emitter.onTimeout(()->sseEmitterMap.remove(guid.toString()));

        return emitter;
    }

    @PostMapping("uploadFile")
    public ResponseEntity<String> uploadLargeFilEntity(@RequestParam("file") MultipartFile file,@RequestParam("uuid") String uuid  ) throws IOException {
        System.out.println(file.getOriginalFilename());
        SseEmitter emiiter =sseEmitterMap.get(uuid);
        fileService.save(file, emiiter, uuid);
        emiiter.send(SseEmitter.event().name("uuid").data("File Uploading completed!"));
        emiiter.complete();
        return ResponseEntity.ok("Works fine!");
    }
    
}
