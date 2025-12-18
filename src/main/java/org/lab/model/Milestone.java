package org.lab.model;

import org.lab.status.MilestoneStatus;
import org.lab.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Milestone(
        Long id,
        String name,
        String description,
        MilestoneStatus status,
        Long projectId,
        LocalDate startDate,
        LocalDate endDate,
        User createdBy,
        LocalDateTime createdAt,
        Set<Long> ticketIds
) {
    public Milestone {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название вехи не может быть пустым");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Веха должна быть привязана к проекту");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты начала и окончания должны быть указаны");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = new MilestoneStatus.Open();
        }
        if (ticketIds == null) {
            ticketIds = Set.of();
        }
    }

    public static Milestone create(String name, String description, Long projectId,
                                   LocalDate startDate, LocalDate endDate, User createdBy) {
        return new Milestone(
                null,
                name,
                description,
                new MilestoneStatus.Open(),
                projectId,
                startDate,
                endDate,
                createdBy,
                LocalDateTime.now(),
                Set.of()
        );
    }

    public Milestone withStatus(MilestoneStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(STR."Невозможно изменить статус с \{status.getDisplayName()} на \{newStatus.getDisplayName()}");
        }

        return new Milestone(id, name, description, newStatus, projectId, startDate, endDate,
                createdBy, createdAt, ticketIds);
    }

    public Milestone addTicket(Long ticketId) {
        var newTicketIds = new HashSet<>(ticketIds);
        newTicketIds.add(ticketId);
        return new Milestone(id, name, description, status, projectId, startDate, endDate,
                createdBy, createdAt, newTicketIds);
    }

    public boolean canBeClosed(List<Ticket> allTickets) {
        var milestoneTickets = allTickets.stream()
                .filter(ticket -> ticketIds.contains(ticket.id()))
                .toList();

        return milestoneTickets.stream().allMatch(Ticket::isCompleted);
    }

    public boolean isActive() {
        return status instanceof MilestoneStatus.Active;
    }

    public boolean isClosed() {
        return status instanceof MilestoneStatus.Closed;
    }
}