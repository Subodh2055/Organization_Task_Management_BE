package com.organization.taskmanagement.clarification.service;

import com.organization.taskmanagement.clarification.dto.AttachmentDto;
import com.organization.taskmanagement.clarification.dto.ClarificationDetailDto;
import com.organization.taskmanagement.clarification.dto.ClarificationDto;
import com.organization.taskmanagement.clarification.dto.ClarificationScope;
import com.organization.taskmanagement.clarification.dto.CommentDto;
import com.organization.taskmanagement.clarification.dto.CreateClarificationRequest;
import com.organization.taskmanagement.clarification.dto.UpdateClarificationRequest;
import com.organization.taskmanagement.clarification.entity.ActivityType;
import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationCategory;
import com.organization.taskmanagement.clarification.entity.ClarificationPriority;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.hasCategory;
import static com.organization.taskmanagement.clarification.repository.ClarificationSpecifications.hasPriority;
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

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final ClarificationRepository clarificationRepository;
    private final ClarificationCommentRepository commentRepository;
    private final ClarificationAttachmentRepository attachmentRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ActivityService activityService;

    public record Filters(ClarificationStatus status, boolean overdueOnly, ClarificationPriority priority,
                          ClarificationCategory category, Long projectId, String search) {
    }

    /** Search and page through the clarifications the user may see. */
    @Transactional(readOnly = true)
    public PageResponse<ClarificationDto> search(User user, ClarificationScope scope, Filters f, Pageable pageable) {
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
        if (f.status() != null) {
            filters.add(hasStatus(f.status()));
        }
        if (f.overdueOnly()) {
            filters.add(overdue(LocalDate.now()));
        }
        if (f.priority() != null) {
            filters.add(hasPriority(f.priority()));
        }
        if (f.category() != null) {
            filters.add(hasCategory(f.category()));
        }
        if (f.projectId() != null) {
            filters.add(inProject(f.projectId()));
        }
        if (f.search() != null && !f.search().isBlank()) {
            filters.add(matches(f.search()));
        }
        return PageResponse.of(clarificationRepository.findAll(Specification.allOf(filters), pageable), ClarificationDto::from);
    }

    /**
     * Staff ask customers and customers ask staff, about a project both of them belong to
     * (see Project#admits: organization and membership rules).
     */
    public ClarificationDto create(CreateClarificationRequest request, User requester) {
        Project project = projectService.getEntity(request.projectId());
        RoleName counterpart = requester.getRole() == null ? null : requester.getRole().counterpart();
        if (counterpart == null) {
            throw ApiException.forbidden("Only staff and customers can raise clarifications");
        }
        if (!project.admits(requester)) {
            throw ApiException.forbidden("You are not a member of this project");
        }
        User requestedTo = userService.getEntity(request.requestedToId());
        if (!requestedTo.hasRole(counterpart)) {
            throw ApiException.badRequest("Staff can only ask customers, and customers can only ask staff");
        }
        if (!isCandidate(counterpart, project, requestedTo.getId())) {
            throw ApiException.badRequest(requestedTo.isActive()
                    ? "That person is not on this project"
                    : "That user's account is deactivated");
        }

        Clarification clarification = new Clarification();
        clarification.setProject(project);
        clarification.setRequestedBy(requester);
        clarification.setRequestedTo(requestedTo);
        clarification.setSubject(request.subject().trim());
        clarification.setDescription(request.description().trim());
        clarification.setExpectedClosureDate(request.expectedClosureDate());
        clarification.setEmailReference(blankToNull(request.emailReference()));
        clarification.setPriority(request.priority() == null ? ClarificationPriority.NORMAL : request.priority());
        clarification.setCategory(request.category() == null ? ClarificationCategory.GENERAL : request.category());
        clarification = clarificationRepository.save(clarification);

        activityService.record(clarification, requester, ActivityType.CREATED, "Asked %s · %s priority · %s%s".formatted(
                requestedTo.getFullName(), clarification.getPriority().label(), clarification.getCategory().label(),
                clarification.getExpectedClosureDate() == null ? "" : " · due " + clarification.getExpectedClosureDate().format(DATE)));
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
                activityService.history(id),
                canAnswer(clarification, user), canParticipate(clarification, user),
                canReassign(clarification, user), canReopen(clarification, user),
                canEditClassification(clarification, user), canChangeDueDate(clarification, user));
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

        activityService.record(clarification, user, ActivityType.ANSWERED, null);
        notificationService.clarificationAnswered(clarification);
        return ClarificationDto.from(clarification);
    }

    /**
     * Changes priority, category or due date of a pending clarification. Priority and category:
     * requester, assignee or admin. Due date: requester or admin. Each change goes into the history,
     * and the assignee is told when someone else changed it.
     */
    public ClarificationDto update(Long id, UpdateClarificationRequest request, User actor) {
        Clarification clarification = getViewable(id, actor);
        if (clarification.getStatus() != ClarificationStatus.PENDING) {
            throw ApiException.conflict("Answered clarifications cannot be changed; reopen it first");
        }
        List<String> changes = new ArrayList<>();

        if (request.priority() != null && request.priority() != clarification.getPriority()) {
            requireClassificationRights(clarification, actor);
            String change = "%s → %s".formatted(clarification.getPriority().label(), request.priority().label());
            clarification.setPriority(request.priority());
            activityService.record(clarification, actor, ActivityType.PRIORITY_CHANGED, change);
            changes.add("priority " + change);
        }
        if (request.category() != null && request.category() != clarification.getCategory()) {
            requireClassificationRights(clarification, actor);
            String change = "%s → %s".formatted(clarification.getCategory().label(), request.category().label());
            clarification.setCategory(request.category());
            activityService.record(clarification, actor, ActivityType.CATEGORY_CHANGED, change);
            changes.add("category " + change);
        }
        LocalDate newDue = Boolean.TRUE.equals(request.clearDueDate()) ? null
                : request.expectedClosureDate() != null ? request.expectedClosureDate() : clarification.getExpectedClosureDate();
        if (!Objects.equals(newDue, clarification.getExpectedClosureDate())) {
            if (!canChangeDueDate(clarification, actor)) {
                throw ApiException.forbidden("Only the requester or an admin can change the due date");
            }
            String change = "%s → %s".formatted(formatDate(clarification.getExpectedClosureDate()), formatDate(newDue));
            clarification.setExpectedClosureDate(newDue);
            clarification.resetReminders();
            activityService.record(clarification, actor, ActivityType.DUE_DATE_CHANGED, change);
            changes.add("due date " + change);
        }

        if (!changes.isEmpty()) {
            clarification.setUpdatedAt(Instant.now());
            if (!actor.getId().equals(clarification.getRequestedTo().getId())) {
                notificationService.clarificationUpdated(clarification, actor, String.join(", ", changes));
            }
        }
        return ClarificationDto.from(clarification);
    }

    /** Who a pending clarification can be handed to: other project members of the assignee's role. */
    @Transactional(readOnly = true)
    public List<UserSummary> reassignCandidates(Long id, User user) {
        Clarification clarification = getViewable(id, user);
        if (!canReassign(clarification, user)) {
            throw ApiException.forbidden("You cannot reassign this clarification");
        }
        Long currentAssignee = clarification.getRequestedTo().getId();
        return userService.candidatesFor(assigneeRole(clarification), clarification.getProject()).stream()
                .filter(candidate -> !candidate.id().equals(currentAssignee))
                .toList();
    }

    /** The requester, the assignee or an admin hands a pending clarification to another eligible person. */
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
        if (!isCandidate(assigneeRole(clarification), clarification.getProject(), newAssigneeId)) {
            throw ApiException.badRequest("That person cannot be asked about this project");
        }
        User next = userService.getEntity(newAssigneeId);
        clarification.setRequestedTo(next);
        clarification.setUpdatedAt(Instant.now());
        clarification.resetReminders();

        String cleanNote = blankToNull(note);
        activityService.record(clarification, actor, ActivityType.REASSIGNED, "From %s to %s%s".formatted(
                previous.getFullName(), next.getFullName(), cleanNote == null ? "" : "\nNote: " + cleanNote));
        notificationService.clarificationReassigned(clarification, actor, previous, cleanNote);
        return ClarificationDto.from(clarification);
    }

    /**
     * The requester (or an admin) reopens an answered clarification when the answer does not settle it.
     * The reason and the previous answer are kept in the history.
     */
    public ClarificationDto reopen(Long id, String reason, User actor) {
        Clarification clarification = getViewable(id, actor);
        if (!canReopen(clarification, actor)) {
            throw ApiException.forbidden(clarification.getStatus() == ClarificationStatus.PENDING
                    ? "This clarification is still open"
                    : "Only the requester or an admin can reopen this clarification");
        }
        activityService.record(clarification, actor, ActivityType.REOPENED, "Reason: %s\nPrevious answer from %s: %s".formatted(
                reason.trim(),
                clarification.getAnsweredBy() == null ? "unknown" : clarification.getAnsweredBy().getFullName(),
                clarification.getAnswer()));

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

    private boolean canEditClassification(Clarification clarification, User user) {
        return clarification.getStatus() == ClarificationStatus.PENDING
                && (user.hasRole(RoleName.ADMIN) || clarification.isParticipant(user));
    }

    private boolean canChangeDueDate(Clarification clarification, User user) {
        return clarification.getStatus() == ClarificationStatus.PENDING
                && (user.hasRole(RoleName.ADMIN) || clarification.getRequestedBy().getId().equals(user.getId()));
    }

    private void requireClassificationRights(Clarification clarification, User user) {
        if (!canEditClassification(clarification, user)) {
            throw ApiException.forbidden("You cannot change this clarification");
        }
    }

    /** The role the clarification is asked of: the other side from the requester. */
    private static RoleName assigneeRole(Clarification clarification) {
        RoleName requesterRole = clarification.getRequestedBy().getRole();
        return requesterRole == null ? RoleName.STAFF : requesterRole.counterpart();
    }

    private boolean isCandidate(RoleName role, Project project, Long userId) {
        return userService.candidatesFor(role, project).stream().anyMatch(candidate -> candidate.id().equals(userId));
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "none" : date.format(DATE);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
