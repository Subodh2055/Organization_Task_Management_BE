package com.organization.taskmanagement.clarification.controller;

import com.organization.taskmanagement.clarification.dto.CommentDto;
import com.organization.taskmanagement.clarification.dto.CommentRequest;
import com.organization.taskmanagement.clarification.service.ClarificationCommentService;
import com.organization.taskmanagement.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Comments are listed by {@code GET /api/clarifications/{id}}. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clarifications/{clarificationId}/comments")
public class ClarificationCommentController {

    private final ClarificationCommentService commentService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(@PathVariable Long clarificationId, @Valid @RequestBody CommentRequest request,
                          Authentication authentication) {
        return commentService.add(clarificationId, request.body(), userService.currentUser(authentication));
    }
}
