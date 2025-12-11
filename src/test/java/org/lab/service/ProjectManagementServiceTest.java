package org.lab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.lab.model.Project;
import org.lab.status.*;
import org.lab.user.User;
import org.lab.user.UserRole;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты сервиса управления проектами")
class ProjectManagementServiceTest {

    private ProjectManagementService service;
    private User manager;
    private User developer;
    private User tester;

    @BeforeEach
    void setUp() {
        service = new ProjectManagementService();
        manager = new User(1L, "Менеджер", "manager@example.com");
        developer = new User(2L, "Разработчик", "dev@example.com");
        tester = new User(3L, "Тестировщик", "tester@example.com");
    }

    @Nested
    @DisplayName("Управление проектами")
    class ProjectManagement {

        @Test
        @DisplayName("Создание проекта")
        void shouldCreateProject() {
            var project = service.createProject("Тестовый проект", "Описание", manager);

            assertAll(
                () -> assertNotNull(project.id()),
                () -> assertEquals("Тестовый проект", project.name()),
                () -> assertEquals(manager, project.manager()),
                () -> assertTrue(service.getProjects().containsKey(project.id()))
            );
        }

        @Test
        @DisplayName("Добавление разработчика в команду")
        void shouldAddDeveloperToTeam() {
            var project = service.createProject("Проект", "Описание", manager);

            service.addTeamMember(project.id(), developer, new UserRole.Developer(developer));

            var updatedProject = service.getProjects().get(project.id());
            assertTrue(updatedProject.teamMembers().containsKey(developer));
        }

        @Test
        @DisplayName("Добавление тимлидера")
        void shouldAddTeamLeader() {
            var project = service.createProject("Проект", "Описание", manager);
            var teamLeader = new User(4L, "Тимлидер", "lead@example.com");

            service.addTeamMember(project.id(), teamLeader, new UserRole.TeamLeader(teamLeader));

            var updatedProject = service.getProjects().get(project.id());
            assertAll(
                () -> assertTrue(updatedProject.teamLeader().isPresent()),
                () -> assertEquals(teamLeader, updatedProject.teamLeader().get())
            );
        }
    }

    @Nested
    @DisplayName("Управление майлстоунами")
    class MilestoneManagement {

        private Project project;

        @BeforeEach
        void setUp() {
            project = service.createProject("Проект", "Описание", manager);
        }

        @Test
        @DisplayName("Создание майлстоуна")
        void shouldCreateMilestone() {
            var milestone = service.createMilestone(
                project.id(),
                "Первый майлстоун",
                "Описание майлстоуна",
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                manager
            );

            assertAll(
                () -> assertNotNull(milestone.id()),
                () -> assertEquals("Первый майлстоун", milestone.name()),
                () -> assertTrue(milestone.status() instanceof MilestoneStatus.Open),
                () -> assertTrue(service.getMilestones().containsKey(milestone.id()))
            );
        }

        @Test
        @DisplayName("Изменение статуса майлстоуна")
        void shouldChangeMilestoneStatus() {
            var milestone = service.createMilestone(
                project.id(), "Майлстоун", "Описание",
                LocalDate.now(), LocalDate.now().plusDays(30), manager
            );

            service.changeMilestoneStatus(milestone.id(), new MilestoneStatus.Active());

            var updatedMilestone = service.getMilestones().get(milestone.id());
            assertTrue(updatedMilestone.status() instanceof MilestoneStatus.Active);
        }

        @Test
        @DisplayName("Нельзя создать второй активный майлстоун")
        void shouldNotAllowMultipleActiveMilestones() {
            var milestone1 = service.createMilestone(
                project.id(), "Майлстоун 1", "Описание",
                LocalDate.now(), LocalDate.now().plusDays(30), manager
            );
            service.changeMilestoneStatus(milestone1.id(), new MilestoneStatus.Active());

            assertThrows(IllegalStateException.class, () ->
                service.createMilestone(
                    project.id(), "Майлстоун 2", "Описание",
                    LocalDate.now().plusDays(31), LocalDate.now().plusDays(60), manager
                )
            );
        }
    }

    @Nested
    @DisplayName("Управление тикетами")
    class TicketManagement {

        private Project project;
        private org.lab.model.Milestone milestone;

        @BeforeEach
        void setUp() {
            project = service.createProject("Проект", "Описание", manager);
            milestone = service.createMilestone(
                project.id(), "Майлстоун", "Описание",
                LocalDate.now(), LocalDate.now().plusDays(30), manager
            );
        }

        @Test
        @DisplayName("Создание тикета")
        void shouldCreateTicket() {
            var ticket = service.createTicket(
                project.id(),
                milestone.id(),
                "Новая задача",
                "Описание задачи",
                manager
            );

            assertAll(
                () -> assertNotNull(ticket.id()),
                () -> assertEquals("Новая задача", ticket.title()),
                () -> assertTrue(ticket.status() instanceof TicketStatus.New),
                () -> assertTrue(service.getTickets().containsKey(ticket.id()))
            );
        }

        @Test
        @DisplayName("Изменение статуса тикета")
        void shouldChangeTicketStatus() {
            var ticket = service.createTicket(
                project.id(), milestone.id(),
                "Задача", "Описание", manager
            );

            service.changeTicketStatus(ticket.id(), new TicketStatus.Accepted());

            var updatedTicket = service.getTickets().get(ticket.id());
            assertTrue(updatedTicket.status() instanceof TicketStatus.Accepted);
        }
    }

    @Nested
    @DisplayName("Функциональное программирование")
    class FunctionalProgramming {

        @Test
        @DisplayName("Поиск пользователей по роли")
        void shouldFindUsersByRole() {
            var project = service.createProject("Проект", "Описание", manager);
            service.addTeamMember(project.id(), developer, new UserRole.Developer(developer));
            service.addTeamMember(project.id(), tester, new UserRole.Tester(tester));

            var developers = service.findUsers(role -> role instanceof UserRole.Developer);
            var testers = service.findUsers(role -> role instanceof UserRole.Tester);

            assertAll(
                () -> assertEquals(1, developers.size()),
                () -> assertTrue(developers.contains(developer)),
                () -> assertEquals(1, testers.size()),
                () -> assertTrue(testers.contains(tester))
            );
        }

        @Test
        @DisplayName("Генерация отчета по проекту")
        void shouldGenerateProjectReport() {
            var project = service.createProject("Проект", "Описание", manager);
            service.addTeamMember(project.id(), developer, new UserRole.Developer(developer));

            var milestone = service.createMilestone(
                project.id(), "Майлстоун", "Описание",
                LocalDate.now(), LocalDate.now().plusDays(30), manager
            );

            service.createTicket(project.id(), milestone.id(), "Задача 1", "Описание", manager);
            service.createTicket(project.id(), milestone.id(), "Задача 2", "Описание", manager);

            service.createBugReport(project.id(), "Баг 1", "Описание бага", developer);

            var report = service.generateProjectReport(project.id());

            assertAll(
                () -> assertEquals(project.name(), report.project().name()),
                () -> assertEquals(1, report.milestones().size()),
                () -> assertEquals(2, report.ticketsByStatus().get(new TicketStatus.New()).size()),
                () -> assertEquals(1, report.bugReportsByStatus().get(new BugReportStatus.New()).size()),
                () -> assertEquals(1L, report.teamMembersByRole().get("Менеджер")),
                () -> assertEquals(1L, report.teamMembersByRole().get("Разработчик"))
            );
        }
    }
}