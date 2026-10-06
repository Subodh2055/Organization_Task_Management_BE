package com.organization.taskmanagement.notification.service;

import com.organization.taskmanagement.clarification.entity.ActivityType;
import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.entity.ClarificationPriority;
import com.organization.taskmanagement.clarification.repository.ClarificationRepository;
import com.organization.taskmanagement.clarification.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Daily reminders for pending clarifications: the assignee hears shortly before the due date
 * (a day before; two days for urgent items), and both sides hear once it is overdue.
 * Each reminder is sent once per assignee and recorded in the clarification's history.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private static final int MAX_LEAD_DAYS = 2;

    private final ClarificationRepository clarificationRepository;
    private final NotificationService notificationService;
    private final ActivityService activityService;

    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    @Transactional
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        Instant now = Instant.now();

        int dueSoon = 0;
        for (Clarification clarification : clarificationRepository.findDueBetweenWithoutReminder(today, today.plusDays(MAX_LEAD_DAYS))) {
            long daysLeft = ChronoUnit.DAYS.between(today, clarification.getExpectedClosureDate());
            ClarificationPriority priority = clarification.getPriority();
            if (daysLeft > priority.reminderLeadDays()) {
                continue;
            }
            String when = daysLeft == 0 ? "today" : daysLeft == 1 ? "tomorrow" : "in " + daysLeft + " days";
            notificationService.dueSoon(clarification, when);
            clarification.setDueReminderSentAt(now);
            activityService.record(clarification, null, ActivityType.REMINDER_SENT,
                    "Due %s reminder sent to %s".formatted(when, clarification.getRequestedTo().getFullName()));
            dueSoon++;
        }

        List<Clarification> overdue = clarificationRepository.findOverdueWithoutReminder(today);
        for (Clarification clarification : overdue) {
            notificationService.overdue(clarification);
            clarification.setOverdueReminderSentAt(now);
            activityService.record(clarification, null, ActivityType.REMINDER_SENT,
                    "Overdue reminder sent to %s and %s".formatted(clarification.getRequestedTo().getFullName(),
                            clarification.getRequestedBy().getFullName()));
        }

        if (dueSoon > 0 || !overdue.isEmpty()) {
            log.info("Sent {} due-soon and {} overdue reminder(s)", dueSoon, overdue.size());
        }
    }
}
