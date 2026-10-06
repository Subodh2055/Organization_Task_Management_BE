package com.organization.taskmanagement.notification.service;

import com.organization.taskmanagement.clarification.entity.Clarification;
import com.organization.taskmanagement.clarification.repository.ClarificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Daily reminders for pending clarifications: the assignee hears the day before the due date,
 * and both sides hear once it is overdue. Each reminder is sent once per assignee.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ClarificationRepository clarificationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    @Transactional
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        Instant now = Instant.now();

        List<Clarification> dueTomorrow = clarificationRepository.findDueOnWithoutReminder(today.plusDays(1));
        for (Clarification clarification : dueTomorrow) {
            notificationService.dueSoon(clarification);
            clarification.setDueReminderSentAt(now);
        }

        List<Clarification> overdue = clarificationRepository.findOverdueWithoutReminder(today);
        for (Clarification clarification : overdue) {
            notificationService.overdue(clarification);
            clarification.setOverdueReminderSentAt(now);
        }

        if (!dueTomorrow.isEmpty() || !overdue.isEmpty()) {
            log.info("Sent {} due-tomorrow and {} overdue reminder(s)", dueTomorrow.size(), overdue.size());
        }
    }
}
