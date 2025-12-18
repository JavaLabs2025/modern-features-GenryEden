package org.lab.status;

public sealed interface BugReportStatus
        permits BugReportStatus.New, BugReportStatus.Fixed, BugReportStatus.Tested, BugReportStatus.Closed {

    record New() implements BugReportStatus {}
    record Fixed() implements BugReportStatus {}
    record Tested() implements BugReportStatus {}
    record Closed() implements BugReportStatus {}

    default boolean canTransitionTo(BugReportStatus newStatus) {
        return switch (this) {
            case New _ -> newStatus instanceof Fixed;
            case Fixed _ -> newStatus instanceof Tested;
            case Tested _ -> newStatus instanceof Closed;
            case Closed _ -> false;
        };
    }

    default String getDisplayName() {
        return switch (this) {
            case New _ -> "Новый";
            case Fixed _ -> "Исправленный";
            case Tested _ -> "Протестированный";
            case Closed _ -> "Закрытый";
        };
    }
}