package org.lab.status;

public sealed interface MilestoneStatus
        permits MilestoneStatus.Open, MilestoneStatus.Active, MilestoneStatus.Closed {

    record Open() implements MilestoneStatus {}
    record Active() implements MilestoneStatus {}
    record Closed() implements MilestoneStatus {}

    default boolean canTransitionTo(MilestoneStatus newStatus) {
        return switch (this) {
            case Open _ -> newStatus instanceof Active;
            case Active _ -> newStatus instanceof Closed;
            case Closed _ -> false;
        };
    }

    default String getDisplayName() {
        return switch (this) {
            case Open _ -> "Открыт";
            case Active _ -> "Активен";
            case Closed _ -> "Закрыт";
        };
    }
}