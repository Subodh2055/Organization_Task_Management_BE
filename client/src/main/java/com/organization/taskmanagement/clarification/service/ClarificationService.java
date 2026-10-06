package com.organization.taskmanagement.clarification.service;

import com.organization.taskmanagement.clarification.dto.AttachmentDto;
import com.organization.taskmanagement.clarification.dto.ClarificationDetailDto;
import com.organization.taskmanagement.clarification.dto.ClarificationDto;
import com.organization.taskmanagement.clarification.dto.ClarificationScope;
import com.organization.taskmanagement.clarification.dto.CommentDto;
import com.organization.taskmanagement.clarification.dto.CreateClarificationRequest;
import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationComment;
import com.organization.taskmanagement.clarification.entity.ClarificationStatus;
import com.organization.taskmanagement.clarification.repository.ClarificationAttachmentRepository;
import com.organization.taskmanagement.clarification.repository.ClarificationCommentRepository;
import com.organization.taskmanagement.clarification.repository.ClarificationRepository;
import com.organization.taskmanagement.common.dto.PageResponse;
import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.notification.service.NotificationService;
import com.organization.taskmanagement.project.entity.Project;
import com.organization.taskmanagement.project.service.ProjectService;
import com.organization.taskmanagement.user.dto.UserSummary;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import com.organization.taskmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.hasStatus;
import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.inProject;
import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.matches;
import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.overdue;
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
                                                 boolean overdueOnly, Long projectId, String search, Pageable pageable) {
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
        if (overdueOnly) {
            filters.add(overdue(LocalDate.now()));
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
                canAnswer(clarification, user), canParticipate(clarification, user),
                canReassign(clarification, user), canReopen(clarification, user));
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

    /** Who a pending clarification can be handed to: the same kind of user as the current assignee. */
    @Transactional(readOnly = true)
    public List<UserSummary> reassignCandidates(Long id, User user) {
        Clarification clarification = getViewable(id, user);
        if (!canReassign(clarification, user)) {
            throw ApiException.forbidden("You cannot reassign this clarification");
        }
        Long currentAssignee = clarification.getRequestedTo().getId();
        return userService.findAssignable(clarification.getRequestedBy(), clarification.getProject().getId()).stream()
                .filter(candidate -> !candidate.id().equals(currentAssignee))
                .toList();
    }

    /**
     * Hands a pending clarification to someone else. The assignee, the requester or an admin can do it;
     * the new person must be someone the requester could have asked about this project.
     */
    public ClarificationDto reassign(Long id, Long newAssigneeId, String note, User actor) {
        Clarification clarification = getViewable(id, actor);
        if (!canReassign(clarification, actor)) {
            throw ApiException.forbidden(clarification.getStatus() == ClarificationStatus.CLOSED
                    ? "Answered clarifications cannot be reassigned"
                    : "Only the requester, the assignee or an admin can reassign this clarification");
        }
        User previous = clarification.getRequestedTo();
        if (previous.getId().equals(newAssigneeId)) {
            throw ApiException.badRequest("It is already assigned to that person");
        }
        boolean allowed = userService.findAssignable(clarification.getRequestedBy(), clarification.getProject().getId())
                .stream().anyMatch(candidate -> candidate.id().equals(newAssigneeId));
        if (!allowed) {
            throw ApiException.badRequest("That person cannot be asked about this project");
        }
        User next = userService.getEntity(newAssigneeId);
        clarification.setRequestedTo(next);
        clarification.setUpdatedAt(Instant.now());
        clarification.resetReminders();

        String cleanNote = blankToNull(note);
        addSystemComment(clarification, actor, "Reassigned from %s to %s.%s".formatted(
                previous.getFullName(), next.getFullName(), cleanNote == null ? "" : "\n\n" + cleanNote));
        notificationService.clarificationReassigned(clarification, actor, previous, cleanNote);
        return ClarificationDto.from(clarification);
    }

    /**
     * The requester (or an admin) reopens an answered clarification when the answer does not settle it.
     * The previous answer is kept in the discussion so nothing is lost.
     */
    public ClarificationDto reopen(Long id, String reason, User actor) {
        Clarification clarification = getViewable(id, actor);
        if (!canReopen(clarification, actor)) {
            throw ApiException.forbidden(clarification.getStatus() == ClarificationStatus.PENDING
                    ? "This clarification is still open"
                    : "Only the requester or an admin can reopen this clarification");
        }
        String previousAnswer = "Previous answer from %s:\n%s".formatted(
                clarification.getAnsweredBy() == null ? "unknown" : clarification.getAnsweredBy().getFullName(),
                clarification.getAnswer());
        addSystemComment(clarification, actor, "Reopened: %s\n\n%s".formatted(reason.trim(), previousAnswer));

        clarification.setStatus(ClarificationStatus.PENDING);
        clarification.setAnswer(null);
        clarification.setAnsweredBy(null);
        clarification.setAnsweredAt(null);
        clarification.setUpdatedAt(Instant.now());
        clarification.resetReminders();

        notificationService.clarificationReopened(clarification, actor, reason.trim());
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

    private boolean canReassign(Clarification clarification, User user) {
        return clarification.getStatus() == ClarificationStatus.PENDING
                && (user.hasRole(RoleName.ADMIN) || clarification.isParticipant(user));
    }

    private boolean canReopen(Clarification clarification, User user) {
        return clarification.getStatus() == ClarificationStatus.CLOSED
                && (user.hasRole(RoleName.ADMIN) || clarification.getRequestedBy().getId().equals(user.getId()));
    }

    /** Records reassign/reopen in the discussion so the history stays visible. */
    private void addSystemComment(Clarification clarification, User author, String body) {
        ClarificationComment comment = new ClarificationComment();
        comment.setClarification(clarification);
        comment.setAuthor(author);
        comment.setBody(body.length() > 4000 ? body.substring(0, 3997) + "..." : body);
        commentRepository.save(comment);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
