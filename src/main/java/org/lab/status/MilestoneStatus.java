package org.lab.status;

public sealed interface MilestoneStatus
        permits MilestoneStatus.Open, MilestoneStatus.Active, MilestoneStatus.Closed {

    record Open() implements MilestoneStatus {}
    record Active() implements MilestoneStatus {}
    record Closed() implements MilestoneStatus {}

    default boolean canTransitionTo(MilestoneStatus newStatus) {
        return switch (this) {
            case Open open -> newStatus instanceof Active;
            case Active active -> newStatus instanceof Closed;
            case Closed closed -> false;
        };
    }

    default String getDisplayName() {
        return switch (this) {
            case Open open -> "Открыт";
            case Active active -> "Активен";
            case Closed closed -> "Закрыт";
        };
    }
}