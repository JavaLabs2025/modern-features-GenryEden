package org.lab.status;

public sealed interface TicketStatus
        permits TicketStatus.New, TicketStatus.Accepted, TicketStatus.InProgress, TicketStatus.Completed {

    record New() implements TicketStatus {}
    record Accepted() implements TicketStatus {}
    record InProgress() implements TicketStatus {}
    record Completed() implements TicketStatus {}

    default boolean canTransitionTo(TicketStatus newStatus) {
        return switch (this) {
            case New _ -> newStatus instanceof Accepted;
            case Accepted _ -> newStatus instanceof InProgress;
            case InProgress _ -> newStatus instanceof Completed;
            case Completed _ -> false;
        };
    }

    default String getDisplayName() {
        return switch (this) {
            case New _ -> "Новый";
            case Accepted _ -> "Принятый";
            case InProgress _ -> "В процессе выполнения";
            case Completed _ -> "Выполнен";
        };
    }
}