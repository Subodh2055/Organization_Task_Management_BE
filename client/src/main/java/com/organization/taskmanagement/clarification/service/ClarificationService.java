package com.organization.taskmanagement.clarification.service;

import com.organization.taskmanagement.clarification.dto.ClarificationDetailDto;
import com.organization.taskmanagement.clarification.dto.ClarificationDto;
import com.organization.taskmanagement.clarification.dto.ClarificationScope;
import com.organization.taskmanagement.clarification.dto.AttachmentDto;
import com.organization.taskmanagement.clarification.dto.CommentDto;
import com.organization.taskmanagement.clarification.dto.CreateClarificationRequest;
import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationStatus;
import com.organization.taskmanagement.clarification.repository.ClarificationAttachmentRepository;
import com.organization.taskmanagement.clarification.repository.ClarificationCommentRepository;
import com.organization.taskmanagement.clarification.repository.ClarificationRepository;
import com.organization.taskmanagement.common.dto.PageResponse;
import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.notification.service.NotificationService;
import com.organization.taskmanagement.project.entity.Project;
import com.organization.taskmanagement.project.service.ProjectService;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import com.organization.taskmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.hasStatus;
import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.inProject;
import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.matches;
import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.requestedBy;
import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.requestedTo;

@Service
@RequiredArgsConstructor
@Transactional
public class ClarificationService {

    private final ClarificationRepository clarificationRepository;
    private final ClarificationCommentRepository commentRepository;
    private final ClarificationAttachmentRepository attachmentRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final NotificationService notificationService;

    /** Search and page through the clarifications the user may see. */
    @Transactional(readOnly = true)
    public PageResponse<ClarificationDto> search(User user, ClarificationScope scope, ClarificationStatus status,
                                                 Long projectId, String search, Pageable pageable) {
        boolean admin = user.hasRole(RoleName.ADMIN);
        ClarificationScope effective = scope != null ? scope : (admin ? ClarificationScope.ALL : ClarificationScope.ASSIGNED);
        if (effective == ClarificationScope.ALL && !admin) {
            throw ApiException.forbidden("Only admins can see every clarification");
        }

        List<Specification<Clarification>> filters = new ArrayList<>();
        switch (effective) {
            case ASSIGNED -> filters.add(requestedTo(user.getId()));
            case REQUESTED -> filters.add(requestedBy(user.getId()));
            case ALL -> { }
        }
        if (status != null) {
            filters.add(hasStatus(status));
        }
        if (projectId != null) {
            filters.add(inProject(projectId));
        }
        if (search != null && !search.isBlank()) {
            filters.add(matches(search));
        }
        return PageResponse.of(clarificationRepository.findAll(Specification.allOf(filters), pageable), ClarificationDto::from);
    }

    /** Staff ask customers and customers ask staff, about a project the customer's organization owns. */
    public ClarificationDto create(CreateClarificationRequest request, User requester) {
        Project project = projectService.getEntity(request.projectId());
        User requestedTo = userService.getEntity(request.requestedToId());

        RoleName counterpart = requester.getRole() == null ? null : requester.getRole().counterpart();
        if (counterpart == null || !requestedTo.hasRole(counterpart)) {
            throw ApiException.badRequest("Staff can only ask customers, and customers can only ask staff");
        }
        if (!requestedTo.isActive()) {
            throw ApiException.badRequest("That user's account is deactivated");
        }
        User customer = requester.hasRole(RoleName.CUSTOMER) ? requester : requestedTo;
        if (customer.getOrganization() != null
                && !customer.getOrganization().getId().equals(project.getOrganization().getId())) {
            throw ApiException.badRequest("The project must belong to the customer's organization");
        }

        Clarification clarification = new Clarification();
        clarification.setProject(project);
        clarification.setRequestedBy(requester);
        clarification.setRequestedTo(requestedTo);
        clarification.setSubject(request.subject().trim());
        clarification.setDescription(request.description().trim());
        clarification.setExpectedClosureDate(request.expectedClosureDate());
        clarification.setEmailReference(blankToNull(request.emailReference()));
        clarification = clarificationRepository.save(clarification);

        notificationService.clarificationRequested(clarification);
        return ClarificationDto.from(clarification);
    }

    @Transactional(readOnly = true)
    public ClarificationDetailDto detail(Long id, User user) {
        Clarification clarification = getViewable(id, user);
        List<CommentDto> comments = commentRepository.findByClarification_IdOrderByCreatedAtAsc(id).stream()
                .map(CommentDto::from).toList();
        List<AttachmentDto> attachments = attachmentRepository.findByClarification_IdOrderByCreatedAtAsc(id).stream()
                .map(AttachmentDto::from).toList();
        return new ClarificationDetailDto(ClarificationDto.from(clarification), comments, attachments,
                canAnswer(clarification, user), canParticipate(clarification, user));
    }

    /** Only the user the clarification was asked of can answer, and only once. */
    public ClarificationDto answer(Long id, String answer, User user) {
        Clarification clarification = getViewable(id, user);
        if (!clarification.getRequestedTo().getId().equals(user.getId())) {
            throw ApiException.forbidden("Only the requested user can answer this clarification");
        }
        if (clarification.getStatus() == ClarificationStatus.CLOSED) {
            throw ApiException.conflict("This clarification has already been answered");
        }
        Instant now = Instant.now();
        clarification.setAnswer(answer.trim());
        clarification.setAnsweredBy(user);
        clarification.setAnsweredAt(now);
        clarification.setUpdatedAt(now);
        clarification.setStatus(ClarificationStatus.CLOSED);

        notificationService.clarificationAnswered(clarification);
        return ClarificationDto.from(clarification);
    }

    /** Admins see everything; others see what they asked or were asked. */
    @Transactional(readOnly = true)
    public Clarification getViewable(Long id, User user) {
        Clarification clarification = clarificationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Clarification " + id + " was not found"));
        if (!user.hasRole(RoleName.ADMIN) && !clarification.isParticipant(user)) {
            throw ApiException.forbidden("You cannot view this clarification");
        }
        return clarification;
    }

    /** Commenting and attaching files: the two participants and admins. */
    public boolean canParticipate(Clarification clarification, User user) {
        return user.hasRole(RoleName.ADMIN) || clarification.isParticipant(user);
    }

    private boolean canAnswer(Clarification clarification, User user) {
        return clarification.getStatus() == ClarificationStatus.PENDING
                && clarification.getRequestedTo().getId().equals(user.getId());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
