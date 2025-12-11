package org.lab.model;

import org.lab.status.BugReportStatus;
import org.lab.user.User;

import java.time.LocalDateTime;
import java.util.Optional;

public record BugReport(
        Long id,
        String title,
        String description,
        BugReportStatus status,
        Long projectId,
        User reportedBy,
        Optional<User> assignedTo,
        LocalDateTime createdAt,
        Optional<LocalDateTime> fixedAt,
        Optional<LocalDateTime> verifiedAt
) {
    public BugReport {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название баг-репорта не может быть пустым");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Баг-репорт должен быть привязан к проекту");
        }
        if (reportedBy == null) {
            throw new IllegalArgumentException("Должен быть указан автор баг-репорта");
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = new BugReportStatus.New();
        }
        if (assignedTo == null) {
            assignedTo = Optional.empty();
        }
        if (fixedAt == null) {
            fixedAt = Optional.empty();
        }
        if (verifiedAt == null) {
            verifiedAt = Optional.empty();
        }
    }

    public static BugReport create(String title, String description, Long projectId, User reportedBy) {
        return new BugReport(
                null,
                title,
                description,
                new BugReportStatus.New(),
                projectId,
                reportedBy,
                Optional.empty(),
                LocalDateTime.now(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public BugReport withStatus(BugReportStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(STR."Невозможно изменить статус с \{status.getDisplayName()} на \{newStatus.getDisplayName()}");
        }

        var fixedAt = newStatus instanceof BugReportStatus.Fixed
                ? Optional.of(LocalDateTime.now())
                : this.fixedAt;

        var verifiedAt = newStatus instanceof BugReportStatus.Tested
                ? Optional.of(LocalDateTime.now())
                : this.verifiedAt;

        return new BugReport(id, title, description, newStatus, projectId, reportedBy,
                assignedTo, createdAt, fixedAt, verifiedAt);
    }

    public BugReport assignTo(User developer) {
        return new BugReport(id, title, description, status, projectId, reportedBy,
                Optional.of(developer), createdAt, fixedAt, verifiedAt);
    }

    public boolean isClosed() {
        return status instanceof BugReportStatus.Closed;
    }
}