package org.lab.status;

public sealed interface TicketStatus
        permits TicketStatus.New, TicketStatus.Accepted, TicketStatus.InProgress, TicketStatus.Completed {

    record New() implements TicketStatus {}
    record Accepted() implements TicketStatus {}
    record InProgress() implements TicketStatus {}
    record Completed() implements TicketStatus {}

    default boolean canTransitionTo(TicketStatus newStatus) {
        return switch (this) {
            case New newStatus1 -> newStatus instanceof Accepted;
            case Accepted accepted -> newStatus instanceof InProgress;
            case InProgress inProgress -> newStatus instanceof Completed;
            case Completed completed -> false;
        };
    }

    default String getDisplayName() {
        return switch (this) {
            case New newStatus -> "Новый";
            case Accepted accepted -> "Принятый";
            case InProgress inProgress -> "В процессе выполнения";
            case Completed completed -> "Выполнен";
        };
    }
}