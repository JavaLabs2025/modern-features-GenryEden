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
    IO.println("Система управления проектами - демонстрация современных возможностей Java");
    IO.println("=" + "=".repeat(80));

    var service = new ProjectManagementService();
    var manager = new User(1L, "Анна Петрова", "anna@company.com");
    var teamLeader = new User(2L, "Иван Сидоров", "ivan@company.com");
    var developer = new User(3L, "Мария Козлова", "maria@company.com");
    var developer2 = new User(4L, "Алексей Иванов", "alexey@company.com");
    var tester = new User(5L, "Елена Смирнова", "elena@company.com");

    demonstrateProjectCreation(service, manager);
    IO.println();

    demonstrateTeamManagement(service, manager, teamLeader, developer, developer2, tester);
    IO.println();

    demonstrateMilestoneAndTicketManagement(service, manager, developer);
    IO.println();

    demonstrateFunctionalProgramming(service);
    IO.println();

    demonstrateRoleBasedFunctionality(service, manager, developer, tester);
    IO.println();

    demonstrateUserFunctions(service, manager, developer, tester);
    IO.println();

    demonstratePatternMatching();

    IO.println("Демонстрация завершена успешно!");
}

private static void demonstrateProjectCreation(ProjectManagementService service, User manager) {
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

private static void demonstrateTeamManagement(ProjectManagementService service, User manager,
                                            User teamLeader, User developer1, User developer2, User tester) {
    IO.println("Управление командой:");

    var projectId = service.getProjects().keySet().iterator().next();

    service.addTeamMember(projectId, teamLeader, new UserRole.TeamLeader(teamLeader));
    service.addTeamMember(projectId, developer1, new UserRole.Developer(developer1));
    service.addTeamMember(projectId, developer2, new UserRole.Developer(developer2));
    service.addTeamMember(projectId, tester, new UserRole.Tester(tester));

    var project = service.getProjects().get(projectId);

    IO.println(STR."   Команда проекта (\{project.teamMembers().size()} участников):");

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

private static void demonstrateMilestoneAndTicketManagement(ProjectManagementService service,
                                                          User manager, User developer) {
    IO.println("Управление майлстоунами и тикетами:");

    var projectId = service.getProjects().keySet().iterator().next();

    var milestone = service.createMilestone(
            projectId,
            "MVP разработка",
            "Первая версия продукта с базовой функциональностью",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            manager
    );

    IO.println(STR."   Майлстоун '\{milestone.name()}' создан (статус: \{milestone.status().getDisplayName()})");

    service.changeMilestoneStatus(milestone.id(), new MilestoneStatus.Active());
    IO.println(STR."   Майлстоун активирован");

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

private static void demonstrateFunctionalProgramming(ProjectManagementService service) {
    IO.println("Функциональное программирование:");

    var projectId = service.getProjects().keySet().iterator().next();

    var developers = service.findUsers(role -> role instanceof UserRole.Developer);
    var testers = service.findUsers(role -> role instanceof UserRole.Tester);

    IO.println(STR."   Найдено разработчиков: \{developers.size()}");
    IO.println(STR."   Найдено тестировщиков: \{testers.size()}");

    var report = service.generateProjectReport(projectId);
    IO.println("   Отчет по проекту:");
    IO.println(report.getStatistics());
}

private static void demonstratePatternMatching() {
    IO.println("Демонстрация pattern matching:");

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
            case MilestoneStatus.Active active -> STR."Майлстоун активен";
            case BugReportStatus.Fixed fixed -> STR."Баг исправлен";
            default -> STR."Неизвестный статус: \{status.getClass().getSimpleName()}";
        };
        IO.println(STR."   \{message}");
    });
}

private static void demonstrateRoleBasedFunctionality(ProjectManagementService service, User manager,
                                                    User developer, User tester) {
    IO.println("Демонстрация ролевых функций:");

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

    IO.println("   Продемонстрированы функции всех ролей");
}

private static void demonstrateUserFunctions(ProjectManagementService service, User manager,
                                           User developer, User tester) {
    IO.println("Демонстрация пользовательских функций:");

    var userProjects = service.getUserProjects(developer);
    IO.println(STR."   Проекты разработчика \{developer.name()}: \{userProjects.size()}");

    var userTickets = service.getUserTickets(developer);
    IO.println(STR."   Тикеты разработчика \{developer.name()}: \{userTickets.size()}");

    var userBugReports = service.getUserBugReports(developer);
    IO.println(STR."   Баг-репорты разработчика \{developer.name()}: \{userBugReports.size()}");

    var bugReportsToFix = service.getBugReportsToFix(developer);
    IO.println(STR."   Баг-репорты для исправления: \{bugReportsToFix.size()}");

    var testerProjects = service.getUserProjects(tester);
    IO.println(STR."   Проекты тестировщика \{tester.name()}: \{testerProjects.size()}");
}

}