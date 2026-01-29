package com.hudi.qqboot.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/document")
public class DocumentController {

    /**
     * 获取docx文档内容
     */
    @GetMapping(value = "/chaoqingWord", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getChaoqingWordDocx() throws IOException {
        // 从classpath获取资源
        ClassPathResource classPathResource = new ClassPathResource("tempfile/test.docx");
        
        if (!classPathResource.exists()) {
            throw new RuntimeException("文件不存在");
        }
        
        InputStreamResource resource = new InputStreamResource(classPathResource.getInputStream());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"chaoqingWord.docx\"")
                .body(resource);
    }
}