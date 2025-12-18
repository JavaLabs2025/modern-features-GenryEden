package org.lab;

import org.lab.model.*;
import org.lab.service.ProjectManagementService;
import org.lab.status.*;
import org.lab.user.User;
import org.lab.user.UserRole;
import org.lab.util.IO;

import java.time.LocalDate;

public class Main {

public static void main(String[] args) {
    IO.println("Система управления проектами");
    IO.println(STR."=\{"=".repeat(80)}");

    var service = new ProjectManagementService();
    var manager = new User(1L, "Анна Петрова", "anna@company.com");
    var teamLeader = new User(2L, "Иван Сидоров", "ivan@company.com");

    var developers = java.util.List.of(
            new User(3L, "Мария Козлова", "maria@company.com"),
            new User(4L, "Алексей Иванов", "alexey@company.com"),
            new User(5L, "Дмитрий Попов", "dmitry@company.com")
    );

    var testers = java.util.List.of(
            new User(6L, "Елена Смирнова", "elena@company.com"),
            new User(7L, "Андрей Волков", "andrey@company.com")
    );

    createNewProject(service, manager);
    IO.println();

    setupProjectTeam(service, manager, teamLeader, developers, testers);
    IO.println();

    planProjectMilestones(service, manager, developers.getFirst());
    IO.println();

    generateProjectReports(service);
    IO.println();

    processWorkflows(service, manager, developers.getFirst(), testers.getFirst());
    IO.println();

    checkUserActivities(service, manager, developers.getFirst(), testers.getFirst());
    IO.println();

    handleStatusUpdates();

    IO.println("Система управления проектами запущена успешно!");
}

private static void createNewProject(ProjectManagementService service, User manager) {
    IO.println("Создание проекта:");

    var project = service.createProject(
            "Интернет-магазин",
            "Разработка современного интернет-магазина с микросервисной архитектурой",
            manager
    );

    IO.println(STR."   Проект '\{project.name()}' создан (ID: \{project.id()})");
    IO.println(STR."   Менеджер: \{manager.name()}");
    IO.println(STR."   Дата создания: \{project.createdAt()}");
}

private static void setupProjectTeam(ProjectManagementService service, User manager,
                                     User teamLeader, java.util.List<User> developers, java.util.List<User> testers) {
    IO.println("Формирование команды проекта:");

    var projectId = service.getProjects().keySet().iterator().next();

    service.addTeamMember(projectId, teamLeader, new UserRole.TeamLeader(teamLeader));

    developers.forEach(dev ->
        service.addTeamMember(projectId, dev, new UserRole.Developer(dev))
    );

    testers.forEach(tester ->
        service.addTeamMember(projectId, tester, new UserRole.Tester(tester))
    );

    var project = service.getProjects().get(projectId);

    IO.println(STR."   Команда проекта (\{project.teamMembers().size()} участников):");
    IO.println(STR."   - Разработчиков: \{developers.size()}");
    IO.println(STR."   - Тестировщиков: \{testers.size()}");

    project.teamMembers().forEach((user, role) -> {
        var permissions = switch (role) {
            case UserRole.Manager m -> m.getPermissions().size();
            case UserRole.TeamLeader tl -> tl.getPermissions().size();
            case UserRole.Developer d -> d.getPermissions().size();
            case UserRole.Tester t -> t.getPermissions().size();
        };
        IO.println(STR."     - \{user.name()} (\{role.getRoleName()}, \{permissions} разрешений)");
    });
}

private static void planProjectMilestones(ProjectManagementService service,
                                          User manager, User developer) {
    IO.println("Планирование вех и задач:");

    var projectId = service.getProjects().keySet().iterator().next();

    var milestone = service.createMilestone(
            projectId,
            "MVP разработка",
            "Первая версия продукта с базовой функциональностью",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            manager
    );

    IO.println(STR."   Веха '\{milestone.name()}' создана (статус: \{milestone.status().getDisplayName()})");

    service.changeMilestoneStatus(milestone.id(), new MilestoneStatus.Active());
    IO.println(STR."   Веха активирована");

    var ticket1 = service.createTicket(projectId, milestone.id(),
            "Реализация аутентификации", "Создать систему входа/регистрации", manager);
    var ticket2 = service.createTicket(projectId, milestone.id(),
            "API каталога товаров", "Разработать REST API для управления товарами", manager);

    IO.println(STR."   Создано тикетов: 2");

    service.changeTicketStatus(ticket1.id(), new TicketStatus.Accepted());
    service.changeTicketStatus(ticket1.id(), new TicketStatus.InProgress());

    var bugReport = service.createBugReport(projectId,
            "Ошибка валидации email", "Некорректная валидация email адресов", developer);
    IO.println(STR."   Баг-репорт создан: '\{bugReport.title()}'");
}

private static void generateProjectReports(ProjectManagementService service) {
    IO.println("Анализ и отчеты по проекту:");

    var projectId = service.getProjects().keySet().iterator().next();

    var developers = service.findUsers(role -> role instanceof UserRole.Developer);
    var testers = service.findUsers(role -> role instanceof UserRole.Tester);

    IO.println(STR."   Найдено разработчиков: \{developers.size()}");
    IO.println(STR."   Найдено тестировщиков: \{testers.size()}");

    var report = service.generateProjectReport(projectId);
    IO.println("   Отчет по проекту:");
    IO.println(report.getStatistics());
}

private static void handleStatusUpdates() {
    IO.println("Обработка изменений статусов:");

    var service = new ProjectManagementService();

    var statuses = java.util.List.of(
            new TicketStatus.New(),
            new TicketStatus.InProgress(),
            new TicketStatus.Completed(),
            new MilestoneStatus.Active(),
            new BugReportStatus.Fixed()
    );

    statuses.forEach(status -> {
        var message = switch (status) {
            case TicketStatus.New newStatus -> STR."Новый тикет требует внимания";
            case TicketStatus.InProgress inProgress -> STR."Тикет в работе";
            case TicketStatus.Completed completed -> STR."Тикет завершен";
            case MilestoneStatus.Active active -> STR."Веха активна";
            case BugReportStatus.Fixed fixed -> STR."Баг исправлен";
            default -> STR."Неизвестный статус: \{status.getClass().getSimpleName()}";
        };
        IO.println(STR."   \{message}");
    });

    try {
        service.validateStatusTransition(new TicketStatus.New(), new TicketStatus.Completed());
        IO.println("   Невалидный переход не обнаружен");
    } catch (IllegalStateException e) {
        IO.println(STR."   Pattern matching validation: \{e.getMessage()}");
    }
}

private static void processWorkflows(ProjectManagementService service, User manager,
                                     User developer, User tester) {
    IO.println("Выполнение рабочих процессов:");

    var projectId = service.getProjects().keySet().iterator().next();

    var tickets = service.getTickets().values().stream().toList();
    if (!tickets.isEmpty()) {
        var ticket = tickets.get(0);

        service.assignDeveloperToTicket(ticket.id(), developer);

        service.executeTicket(ticket.id(), developer);
    }

    var bugReports = service.getBugReports().values().stream().toList();
    if (!bugReports.isEmpty()) {
        var bugReport = bugReports.get(0);

        service.assignBugReportToDeveloper(bugReport.id(), developer);

        service.fixBugReport(bugReport.id(), developer);

        service.testBugReportFix(bugReport.id(), tester);

        service.closeBugReport(bugReport.id(), manager);
    }

    IO.println("   Рабочие процессы выполнены");
}

private static void checkUserActivities(ProjectManagementService service, User manager,
                                        User developer, User tester) {
    IO.println("Проверка активности пользователей:");

    var projectId = service.getProjects().keySet().iterator().next();

    var managerDescription = service.getUserRoleDescription(manager, projectId);
    IO.println(STR."   \{managerDescription}");

    var developerDescription = service.getUserRoleDescription(developer, projectId);
    IO.println(STR."   \{developerDescription}");

    var testerDescription = service.getUserRoleDescription(tester, projectId);
    IO.println(STR."   \{testerDescription}");

    var userProjects = service.getUserProjects(developer);
    IO.println(STR."   Проекты разработчика: \{userProjects.size()}");

    var bugReportsToFix = service.getBugReportsToFix(developer);
    IO.println(STR."   Баг-репорты для исправления: \{bugReportsToFix.size()}");
}

}