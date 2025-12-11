package org.lab.status;

public sealed interface BugReportStatus
        permits BugReportStatus.New, BugReportStatus.Fixed, BugReportStatus.Tested, BugReportStatus.Closed {

    record New() implements BugReportStatus {}
    record Fixed() implements BugReportStatus {}
    record Tested() implements BugReportStatus {}
    record Closed() implements BugReportStatus {}

    default boolean canTransitionTo(BugReportStatus newStatus) {
        return switch (this) {
            case New newStatus1 -> newStatus instanceof Fixed;
            case Fixed fixed -> newStatus instanceof Tested;
            case Tested tested -> newStatus instanceof Closed;
            case Closed closed -> false;
        };
    }

    default String getDisplayName() {
        return switch (this) {
            case New newStatus -> "Новый";
            case Fixed fixed -> "Исправленный";
            case Tested tested -> "Протестированный";
            case Closed closed -> "Закрытый";
        };
    }
}