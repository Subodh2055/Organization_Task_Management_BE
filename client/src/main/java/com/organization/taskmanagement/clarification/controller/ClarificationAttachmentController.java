package com.organization.taskmanagement.clarification.controller;

import com.organization.taskmanagement.clarification.dto.AttachmentDto;
import com.organization.taskmanagement.clarification.service.ClarificationAttachmentService;
import com.organization.taskmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/** Attachments are listed by {@code GET /api/clarifications/{id}}. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clarifications/{clarificationId}/attachments")
public class ClarificationAttachmentController {

    private final ClarificationAttachmentService attachmentService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentDto upload(@PathVariable Long clarificationId, @RequestParam("file") MultipartFile file,
                                Authentication authentication) {
        return attachmentService.upload(clarificationId, file, userService.currentUser(authentication));
    }

    /** Always served as a download, never rendered inline. */
    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> download(@PathVariable Long clarificationId, @PathVariable Long attachmentId,
                                             Authentication authentication) {
        ClarificationAttachmentService.Download download =
                attachmentService.download(clarificationId, attachmentId, userService.currentUser(authentication));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.attachment().getFileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentLength(download.attachment().getSize())
                .body(download.resource());
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long clarificationId, @PathVariable Long attachmentId, Authentication authentication) {
        attachmentService.delete(clarificationId, attachmentId, userService.currentUser(authentication));
    }
}
