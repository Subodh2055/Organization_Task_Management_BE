package com.organization.taskmanagement.clarification.controller;

import com.organization.taskmanagement.clarification.dto.AnswerRequest;
import com.organization.taskmanagement.clarification.dto.ClarificationDetailDto;
import com.organization.taskmanagement.clarification.dto.ClarificationDto;
import com.organization.taskmanagement.clarification.dto.ClarificationScope;
import com.organization.taskmanagement.clarification.dto.CreateClarificationRequest;
import com.organization.taskmanagement.clarification.dto.ReassignRequest;
import com.organization.taskmanagement.clarification.dto.ReopenRequest;
import com.organization.taskmanagement.user.dto.UserSummary;
import com.organization.taskmanagement.clarification.entity.ClarificationStatus;
import com.organization.taskmanagement.clarification.service.ClarificationService;
import com.organization.taskmanagement.common.dto.PageResponse;
import com.organization.taskmanagement.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clarifications")
public class ClarificationController {

    private final ClarificationService clarificationService;
    private final UserService userService;

    /**
     * Paged, newest activity first. scope: ALL (admin), ASSIGNED (asked of me) or REQUESTED (asked by me);
     * optional status, overdue (pending past its due date), projectId and free-text search.
     */
    @GetMapping
    public PageResponse<ClarificationDto> search(@RequestParam(required = false) ClarificationScope scope,
                                                 @RequestParam(required = false) ClarificationStatus status,
                                                 @RequestParam(defaultValue = "false") boolean overdue,
                                                 @RequestParam(required = false) Long projectId,
                                                 @RequestParam(required = false) String search,
                                                 @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                 Authentication authentication) {
        return clarificationService.search(userService.currentUser(authentication), scope, status, overdue, projectId,
                search, pageable);
    }

    /** Staff or customer raises a clarification; the requester and date are set by the server. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClarificationDto create(@Valid @RequestBody CreateClarificationRequest request, Authentication authentication) {
        return clarificationService.create(request, userService.currentUser(authentication));
    }

    @GetMapping("/{id}")
    public ClarificationDetailDto detail(@PathVariable Long id, Authentication authentication) {
        return clarificationService.detail(id, userService.currentUser(authentication));
    }

    @PostMapping("/{id}/answer")
    public ClarificationDto answer(@PathVariable Long id, @Valid @RequestBody AnswerRequest request,
                                   Authentication authentication) {
        return clarificationService.answer(id, request.answer(), userService.currentUser(authentication));
    }

    /** People a pending clarification can be handed to. */
    @GetMapping("/{id}/reassign-candidates")
    public List<UserSummary> reassignCandidates(@PathVariable Long id, Authentication authentication) {
        return clarificationService.reassignCandidates(id, userService.currentUser(authentication));
    }

    /** The requester, the assignee or an admin hands a pending clarification to someone else. */
    @PostMapping("/{id}/reassign")
    public ClarificationDto reassign(@PathVariable Long id, @Valid @RequestBody ReassignRequest request,
                                     Authentication authentication) {
        return clarificationService.reassign(id, request.requestedToId(), request.note(), userService.currentUser(authentication));
    }

    /** The requester or an admin reopens an answered clarification. */
    @PostMapping("/{id}/reopen")
    public ClarificationDto reopen(@PathVariable Long id, @Valid @RequestBody ReopenRequest request,
                                   Authentication authentication) {
        return clarificationService.reopen(id, request.reason(), userService.currentUser(authentication));
    }
}
