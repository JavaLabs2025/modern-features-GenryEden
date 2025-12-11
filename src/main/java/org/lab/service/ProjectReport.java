package org.lab.service;

import org.lab.model.*;
import org.lab.status.*;

import java.util.List;
import java.util.Map;

public record ProjectReport(
        Project project,
        List<Milestone> milestones,
        Map<TicketStatus, List<Ticket>> ticketsByStatus,
        Map<BugReportStatus, List<BugReport>> bugReportsByStatus,
        Map<String, Long> teamMembersByRole
) {

    public String getStatistics() {
        var totalTickets = ticketsByStatus.values().stream()
                .mapToInt(List::size)
                .sum();

        var completedTickets = ticketsByStatus.getOrDefault(new TicketStatus.Completed(), List.of()).size();

        var totalBugReports = bugReportsByStatus.values().stream()
                .mapToInt(List::size)
                .sum();

        var closedBugReports = bugReportsByStatus.getOrDefault(new BugReportStatus.Closed(), List.of()).size();

        var teamSize = project.teamMembers().size();

        return STR."""
                Статистика проекта "\{project.name()}"

                Команда: \{teamSize} участников
                \{formatTeamByRole()}

                Майлстоуны: \{milestones.size()}

                Тикеты: \{totalTickets} (завершено: \{completedTickets})
                \{formatTicketsByStatus()}

                Баг-репорты: \{totalBugReports} (закрыто: \{closedBugReports})
                \{formatBugReportsByStatus()}
                """;
    }

    private String formatTeamByRole() {
        return teamMembersByRole.entrySet().stream()
                .map(entry -> STR."   \{entry.getKey()}: \{entry.getValue()}")
                .reduce("", (acc, role) -> acc + "\n" + role);
    }

    private String formatTicketsByStatus() {
        return ticketsByStatus.entrySet().stream()
                .map(entry -> STR."   \{entry.getKey().getDisplayName()}: \{entry.getValue().size()}")
                .reduce("", (acc, status) -> acc + "\n" + status);
    }

    private String formatBugReportsByStatus() {
        return bugReportsByStatus.entrySet().stream()
                .map(entry -> STR."   \{entry.getKey().getDisplayName()}: \{entry.getValue().size()}")
                .reduce("", (acc, status) -> acc + "\n" + status);
    }
}