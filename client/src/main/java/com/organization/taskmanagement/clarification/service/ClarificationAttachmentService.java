package com.organization.taskmanagement.clarification.service;

import com.organization.taskmanagement.clarification.dto.AttachmentDto;
import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ActivityType;
import com.organization.taskmanagement.clarification.entity.ClarificationAttachment;
import com.organization.taskmanagement.clarification.repository.ClarificationAttachmentRepository;
import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.storage.FileStorageService;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ClarificationAttachmentService {

    private final ClarificationService clarificationService;
    private final ClarificationAttachmentRepository attachmentRepository;
    private final FileStorageService storage;
    private final ActivityService activityService;

    public record Download(ClarificationAttachment attachment, Resource resource) {
    }

    public AttachmentDto upload(Long clarificationId, MultipartFile file, User user) {
        Clarification clarification = clarificationService.getViewable(clarificationId, user);
        if (!clarificationService.canParticipate(clarification, user)) {
            throw ApiException.forbidden("You cannot add files to this clarification");
        }
        ClarificationAttachment attachment = new ClarificationAttachment();
        attachment.setClarification(clarification);
        attachment.setUploadedBy(user);
        attachment.setFileName(cleanFileName(file.getOriginalFilename()));
        attachment.setContentType(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setStoredName(storage.store(file));
        clarification.setUpdatedAt(Instant.now());
        attachment = attachmentRepository.save(attachment);
        activityService.record(clarification, user, ActivityType.ATTACHMENT_ADDED, attachment.getFileName());
        return AttachmentDto.from(attachment);
    }

    @Transactional(readOnly = true)
    public Download download(Long clarificationId, Long attachmentId, User user) {
        clarificationService.getViewable(clarificationId, user);
        ClarificationAttachment attachment = find(clarificationId, attachmentId);
        return new Download(attachment, storage.load(attachment.getStoredName()));
    }

    /** The uploader or an admin can remove a file. */
    public void delete(Long clarificationId, Long attachmentId, User user) {
        clarificationService.getViewable(clarificationId, user);
        ClarificationAttachment attachment = find(clarificationId, attachmentId);
        if (!user.hasRole(RoleName.ADMIN) && !attachment.getUploadedBy().getId().equals(user.getId())) {
            throw ApiException.forbidden("Only the uploader or an admin can delete this file");
        }
        activityService.record(attachment.getClarification(), user, ActivityType.ATTACHMENT_REMOVED, attachment.getFileName());
        attachmentRepository.delete(attachment);
        storage.delete(attachment.getStoredName());
    }

    private ClarificationAttachment find(Long clarificationId, Long attachmentId) {
        return attachmentRepository.findByIdAndClarification_Id(attachmentId, clarificationId)
                .orElseThrow(() -> ApiException.notFound("Attachment " + attachmentId + " was not found"));
    }

    /** Keeps only the file name part, without any path from the client. */
    private static String cleanFileName(String original) {
        String name = StringUtils.getFilename(StringUtils.cleanPath(original == null ? "" : original.replace('\\', '/')));
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }
}
