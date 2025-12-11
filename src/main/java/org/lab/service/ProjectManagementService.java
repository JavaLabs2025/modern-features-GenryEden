package org.lab.service;

import org.lab.model.*;
import org.lab.status.*;
import org.lab.user.User;
import org.lab.user.UserRole;
import org.lab.util.IO;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProjectManagementService {

    private final Map<Long, Project> projects = new HashMap<>();
    private final Map<Long, Milestone> milestones = new HashMap<>();
    private final Map<Long, Ticket> tickets = new HashMap<>();
    private final Map<Long, BugReport> bugReports = new HashMap<>();

    private Long nextProjectId = 1L;
    private Long nextMilestoneId = 1L;
    private Long nextTicketId = 1L;
    private Long nextBugReportId = 1L;

    public Project createProject(String name, String description, User manager) {
        var project = Project.create(name, description, manager);
        var projectWithId = new Project(
                nextProjectId++,
                project.name(),
                project.description(),
                project.teamMembers(),
                project.teamLeader(),
                project.manager(),
                project.milestoneIds(),
                project.bugReportIds(),
                project.createdAt()
        );

        projects.put(projectWithId.id(), projectWithId);
        return projectWithId;
    }

    public void addTeamMember(Long projectId, User user, UserRole role) {
        var project = getProjectById(projectId);

        var canAddMember = switch (role) {
            case UserRole.Manager manager -> false;
            case UserRole.TeamLeader teamLeader -> project.teamLeader().isEmpty();
            case UserRole.Developer developer -> true;
            case UserRole.Tester tester -> true;
        };

        if (!canAddMember) {
            var message = switch (role) {
                case UserRole.Manager manager -> "В проекте может быть только один менеджер";
                case UserRole.TeamLeader teamLeader -> "Тимлидер уже назначен";
                case UserRole.Developer developer -> "Неожиданная ошибка";
                case UserRole.Tester tester -> "Неожиданная ошибка";
            };
            throw new IllegalStateException(message);
        }

        var updatedProject = project.addTeamMember(user, role);

        if (role instanceof UserRole.TeamLeader) {
            updatedProject = updatedProject.assignTeamLeader(user);
        }

        projects.put(projectId, updatedProject);
    }

    public Milestone createMilestone(Long projectId, String name, String description,
                                   LocalDate startDate, LocalDate endDate, User createdBy) {
        var project = getProjectById(projectId);

        var activeMilestonesCount = project.getActiveMilestonesCount(getAllMilestones());
        if (activeMilestonesCount > 0) {
            throw new IllegalStateException("В проекте может быть только один активный майлстоун");
        }

        var milestone = Milestone.create(name, description, projectId, startDate, endDate, createdBy);
        var milestoneWithId = new Milestone(
                nextMilestoneId++,
                milestone.name(),
                milestone.description(),
                milestone.status(),
                milestone.projectId(),
                milestone.startDate(),
                milestone.endDate(),
                milestone.createdBy(),
                milestone.createdAt(),
                milestone.ticketIds()
        );

        milestones.put(milestoneWithId.id(), milestoneWithId);
        projects.put(projectId, project.addMilestone(milestoneWithId.id()));

        return milestoneWithId;
    }

    public void changeMilestoneStatus(Long milestoneId, MilestoneStatus newStatus) {
        var milestone = getMilestoneById(milestoneId);

        var validationResult = switch (newStatus) {
            case MilestoneStatus.Open open -> "Майлстоун уже открыт";
            case MilestoneStatus.Active active ->
                milestone.status() instanceof MilestoneStatus.Open ? null : "Можно активировать только открытые майлстоуны";
            case MilestoneStatus.Closed closed -> {
                if (!(milestone.status() instanceof MilestoneStatus.Active)) {
                    yield "Можно закрыть только активные майлстоуны";
                }
                if (!milestone.canBeClosed(getAllTickets())) {
                    yield "Нельзя закрыть майлстоун с незавершенными тикетами";
                }
                yield null;
            }
        };

        if (validationResult != null) {
            throw new IllegalStateException(validationResult);
        }

        var updatedMilestone = milestone.withStatus(newStatus);
        milestones.put(milestoneId, updatedMilestone);
    }

    public Ticket createTicket(Long projectId, Long milestoneId, String title, String description, User createdBy) {
        var project = getProjectById(projectId);
        var milestone = getMilestoneById(milestoneId);

        if (!milestone.projectId().equals(projectId)) {
            throw new IllegalArgumentException("Майлстоун не принадлежит указанному проекту");
        }

        var ticket = Ticket.create(title, description, projectId, milestoneId, createdBy);
        var ticketWithId = new Ticket(
                nextTicketId++,
                ticket.title(),
                ticket.description(),
                ticket.status(),
                ticket.projectId(),
                ticket.milestoneId(),
                ticket.assignedDevelopers(),
                ticket.createdBy(),
                ticket.createdAt(),
                ticket.completedAt()
        );

        tickets.put(ticketWithId.id(), ticketWithId);
        milestones.put(milestoneId, milestone.addTicket(ticketWithId.id()));

        return ticketWithId;
    }

    public void changeTicketStatus(Long ticketId, TicketStatus newStatus) {
        var ticket = getTicketById(ticketId);
        var updatedTicket = ticket.withStatus(newStatus);
        tickets.put(ticketId, updatedTicket);

        var notification = generateStatusChangeNotification(updatedTicket, newStatus);
        IO.println(notification);
    }

    public void assignDeveloperToTicket(Long ticketId, User developer) {
        var ticket = getTicketById(ticketId);
        var project = getProjectById(ticket.projectId());

        if (!project.getDevelopers().contains(developer)) {
            throw new IllegalStateException("Разработчик не участвует в этом проекте");
        }

        var currentDevelopers = ticket.assignedDevelopers();
        var newDevelopers = new java.util.HashSet<>(currentDevelopers);
        newDevelopers.add(developer);

        var updatedTicket = ticket.assignToDevelopers(newDevelopers);
        tickets.put(ticketId, updatedTicket);

        IO.println(STR."Разработчик \{developer.name()} назначен на тикет \"\{ticket.title()}\"");
    }

    public void assignDevelopersToTicket(Long ticketId, Set<User> developers) {
        var ticket = getTicketById(ticketId);
        var project = getProjectById(ticket.projectId());
        var projectDevelopers = project.getDevelopers();

        var invalidDevelopers = developers.stream()
                .filter(dev -> !projectDevelopers.contains(dev))
                .toList();

        if (!invalidDevelopers.isEmpty()) {
            throw new IllegalStateException(STR."Следующие разработчики не участвуют в проекте: \{invalidDevelopers}");
        }

        var updatedTicket = ticket.assignToDevelopers(developers);
        tickets.put(ticketId, updatedTicket);

        IO.println(STR."Назначено разработчиков на тикет \"\{ticket.title()}\": \{developers.size()}");
    }

    public BugReport createBugReport(Long projectId, String title, String description, User reportedBy) {
        var project = getProjectById(projectId);

        var bugReport = BugReport.create(title, description, projectId, reportedBy);
        var bugReportWithId = new BugReport(
                nextBugReportId++,
                bugReport.title(),
                bugReport.description(),
                bugReport.status(),
                bugReport.projectId(),
                bugReport.reportedBy(),
                bugReport.assignedTo(),
                bugReport.createdAt(),
                bugReport.fixedAt(),
                bugReport.verifiedAt()
        );

        bugReports.put(bugReportWithId.id(), bugReportWithId);
        projects.put(projectId, project.addBugReport(bugReportWithId.id()));

        return bugReportWithId;
    }

    public ProjectReport generateProjectReport(Long projectId) {
        var project = getProjectById(projectId);

        var projectMilestones = getAllMilestones().stream()
                .filter(milestone -> project.milestoneIds().contains(milestone.id()))
                .toList();

        var projectTickets = getAllTickets().stream()
                .filter(ticket -> ticket.projectId().equals(projectId))
                .toList();

        var projectBugReports = getAllBugReports().stream()
                .filter(bugReport -> bugReport.projectId().equals(projectId))
                .toList();

        var ticketsByStatus = projectTickets.stream()
                .collect(Collectors.groupingBy(Ticket::status));

        var bugReportsByStatus = projectBugReports.stream()
                .collect(Collectors.groupingBy(BugReport::status));

        var roleStats = project.teamMembers().values().stream()
                .collect(Collectors.groupingBy(
                        role -> role.getRoleName(),
                        Collectors.counting()
                ));

        return new ProjectReport(
                project,
                projectMilestones,
                ticketsByStatus,
                bugReportsByStatus,
                roleStats
        );
    }

    public List<User> findUsers(Predicate<UserRole> roleFilter) {
        return projects.values().stream()
                .flatMap(project -> project.teamMembers().entrySet().stream())
                .filter(entry -> roleFilter.test(entry.getValue()))
                .map(Map.Entry::getKey)
                .distinct()
                .toList();
    }

    public List<Project> getUserProjects(User user) {
        return projects.values().stream()
                .filter(project -> project.teamMembers().containsKey(user))
                .toList();
    }

    public List<Ticket> getUserTickets(User user) {
        return tickets.values().stream()
                .filter(ticket -> ticket.assignedDevelopers().contains(user) || ticket.createdBy().equals(user))
                .toList();
    }

    public List<BugReport> getUserBugReports(User user) {
        return bugReports.values().stream()
                .filter(bugReport -> bugReport.reportedBy().equals(user) ||
                        bugReport.assignedTo().map(assignedUser -> assignedUser.equals(user)).orElse(false))
                .toList();
    }

    public List<BugReport> getBugReportsToFix(User developer) {
        return bugReports.values().stream()
                .filter(bugReport -> bugReport.assignedTo().map(assignedUser -> assignedUser.equals(developer)).orElse(false))
                .filter(bugReport -> !(bugReport.status() instanceof BugReportStatus.Closed))
                .toList();
    }

    public void assignBugReportToDeveloper(Long bugReportId, User developer) {
        var bugReport = getBugReportById(bugReportId);
        var project = getProjectById(bugReport.projectId());

        if (!project.getDevelopers().contains(developer)) {
            throw new IllegalStateException("Разработчик не участвует в этом проекте");
        }

        var updatedBugReport = bugReport.assignTo(developer);
        bugReports.put(bugReportId, updatedBugReport);

        IO.println(STR."Баг-репорт \"\{bugReport.title()}\" назначен разработчику \{developer.name()}");
    }

    public void changeBugReportStatus(Long bugReportId, BugReportStatus newStatus) {
        var bugReport = getBugReportById(bugReportId);
        var updatedBugReport = bugReport.withStatus(newStatus);
        bugReports.put(bugReportId, updatedBugReport);

        var statusMessage = switch (newStatus) {
            case BugReportStatus.New newStatus1 -> "переведен в статус 'Новый'";
            case BugReportStatus.Fixed fixed -> "исправлен";
            case BugReportStatus.Tested tested -> "протестирован";
            case BugReportStatus.Closed closed -> "закрыт";
        };

        IO.println(STR."Баг-репорт \"\{bugReport.title()}\" \{statusMessage}");
    }

    public void executeTicket(Long ticketId, User developer) {
        var ticket = getTicketById(ticketId);

        if (!ticket.assignedDevelopers().contains(developer)) {
            throw new IllegalStateException("Разработчик не назначен на этот тикет");
        }

        var currentStatus = ticket.status();
        var newStatus = switch (currentStatus) {
            case TicketStatus.New newTicket -> new TicketStatus.Accepted();
            case TicketStatus.Accepted accepted -> new TicketStatus.InProgress();
            case TicketStatus.InProgress inProgress -> new TicketStatus.Completed();
            case TicketStatus.Completed completed ->
                throw new IllegalStateException("Тикет уже выполнен");
        };

        changeTicketStatus(ticketId, newStatus);
    }

    public void fixBugReport(Long bugReportId, User developer) {
        var bugReport = getBugReportById(bugReportId);

        if (bugReport.assignedTo().isEmpty() || !bugReport.assignedTo().get().equals(developer)) {
            throw new IllegalStateException("Разработчик не назначен на этот баг-репорт");
        }

        if (!(bugReport.status() instanceof BugReportStatus.New)) {
            throw new IllegalStateException("Можно исправлять только новые баг-репорты");
        }

        changeBugReportStatus(bugReportId, new BugReportStatus.Fixed());
    }

    public void testBugReportFix(Long bugReportId, User tester) {
        var bugReport = getBugReportById(bugReportId);
        var project = getProjectById(bugReport.projectId());

        if (!project.getTesters().contains(tester)) {
            throw new IllegalStateException("Тестировщик не участвует в этом проекте");
        }

        if (!(bugReport.status() instanceof BugReportStatus.Fixed)) {
            throw new IllegalStateException("Можно тестировать только исправленные баг-репорты");
        }

        changeBugReportStatus(bugReportId, new BugReportStatus.Tested());
    }

    public void closeBugReport(Long bugReportId, User manager) {
        var bugReport = getBugReportById(bugReportId);
        var project = getProjectById(bugReport.projectId());

        var userRole = project.teamMembers().get(manager);
        if (!(userRole instanceof UserRole.Manager)) {
            throw new IllegalStateException("Закрывать баг-репорты может только менеджер");
        }

        if (!(bugReport.status() instanceof BugReportStatus.Tested)) {
            throw new IllegalStateException("Можно закрывать только протестированные баг-репорты");
        }

        changeBugReportStatus(bugReportId, new BugReportStatus.Closed());
    }

    private String generateStatusChangeNotification(Ticket ticket, TicketStatus newStatus) {
        var statusMessage = switch (newStatus) {
            case TicketStatus.New newStatus1 -> "создан";
            case TicketStatus.Accepted accepted -> "принят к работе";
            case TicketStatus.InProgress inProgress -> "взят в работу";
            case TicketStatus.Completed completed -> "завершен";
        };

        return STR."Тикет \"\{ticket.title()}\" \{statusMessage}";
    }

    private Project getProjectById(Long projectId) {
        return Optional.ofNullable(projects.get(projectId))
                .orElseThrow(() -> new IllegalArgumentException(STR."Проект с ID \{projectId} не найден"));
    }

    private Milestone getMilestoneById(Long milestoneId) {
        return Optional.ofNullable(milestones.get(milestoneId))
                .orElseThrow(() -> new IllegalArgumentException(STR."Майлстоун с ID \{milestoneId} не найден"));
    }

    private Ticket getTicketById(Long ticketId) {
        return Optional.ofNullable(tickets.get(ticketId))
                .orElseThrow(() -> new IllegalArgumentException(STR."Тикет с ID \{ticketId} не найден"));
    }

    private BugReport getBugReportById(Long bugReportId) {
        return Optional.ofNullable(bugReports.get(bugReportId))
                .orElseThrow(() -> new IllegalArgumentException(STR."Баг-репорт с ID \{bugReportId} не найден"));
    }

    private List<Milestone> getAllMilestones() {
        return new ArrayList<>(milestones.values());
    }

    private List<Ticket> getAllTickets() {
        return new ArrayList<>(tickets.values());
    }

    private List<BugReport> getAllBugReports() {
        return new ArrayList<>(bugReports.values());
    }

    public Map<Long, Project> getProjects() {
        return Map.copyOf(projects);
    }

    public Map<Long, Milestone> getMilestones() {
        return Map.copyOf(milestones);
    }

    public Map<Long, Ticket> getTickets() {
        return Map.copyOf(tickets);
    }

    public Map<Long, BugReport> getBugReports() {
        return Map.copyOf(bugReports);
    }
}