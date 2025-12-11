package org.lab.model;

import org.lab.status.TicketStatus;
import org.lab.user.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public record Ticket(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Long projectId,
        Long milestoneId,
        Set<User> assignedDevelopers,
        User createdBy,
        LocalDateTime createdAt,
        Optional<LocalDateTime> completedAt
) {
    public Ticket {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название тикета не может быть пустым");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Тикет должен быть привязан к проекту");
        }
        if (milestoneId == null) {
            throw new IllegalArgumentException("Тикет должен быть привязан к майлстоуну");
        }
        if (assignedDevelopers == null) {
            assignedDevelopers = Set.of();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = new TicketStatus.New();
        }
        if (completedAt == null) {
            completedAt = Optional.empty();
        }
    }

    public static Ticket create(String title, String description, Long projectId, Long milestoneId, User createdBy) {
        return new Ticket(
                null,
                title,
                description,
                new TicketStatus.New(),
                projectId,
                milestoneId,
                Set.of(),
                createdBy,
                LocalDateTime.now(),
                Optional.empty()
        );
    }

    public Ticket withStatus(TicketStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(STR."Невозможно изменить статус с \{status.getDisplayName()} на \{newStatus.getDisplayName()}");
        }

        var completedAt = newStatus instanceof TicketStatus.Completed
                ? Optional.of(LocalDateTime.now())
                : this.completedAt;

        return new Ticket(id, title, description, newStatus, projectId, milestoneId,
                assignedDevelopers, createdBy, createdAt, completedAt);
    }

    public Ticket assignToDevelopers(Set<User> developers) {
        return new Ticket(id, title, description, status, projectId, milestoneId,
                developers, createdBy, createdAt, completedAt);
    }

    public boolean isCompleted() {
        return status instanceof TicketStatus.Completed;
    }
}