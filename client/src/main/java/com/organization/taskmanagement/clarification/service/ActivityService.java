package com.organization.taskmanagement.clarification.service;

import com.organization.taskmanagement.clarification.dto.ActivityDto;
import com.organization.taskmanagement.clarification.entity.ActivityType;
import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationActivity;
import com.organization.taskmanagement.clarification.repository.ClarificationActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.organization.taskmanagement.user.entity.User;

import java.util.List;

/** Writes and reads a clarification's history. Called inside the caller's transaction. */
@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ClarificationActivityRepository activityRepository;

    /** actor is null for system actions. */
    public void record(Clarification clarification, User actor, ActivityType type, String details) {
        ClarificationActivity activity = new ClarificationActivity();
        activity.setClarification(clarification);
        activity.setActor(actor);
        activity.setType(type);
        activity.setDetails(details == null || details.length() <= 4000 ? details : details.substring(0, 3997) + "...");
        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> history(Long clarificationId) {
        return activityRepository.findByClarification_IdOrderByCreatedAtAscIdAsc(clarificationId).stream()
                .map(ActivityDto::from).toList();
    }
}
