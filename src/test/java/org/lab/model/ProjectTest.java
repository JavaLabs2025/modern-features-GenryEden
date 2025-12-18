package org.lab.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.lab.user.User;
import org.lab.user.UserRole;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты модели Project")
class ProjectTest {

    private final User manager = new User(1L, "Иван Петров", "ivan@example.com");
    private final User developer = new User(2L, "Мария Иванова", "maria@example.com");

    @Nested
    @DisplayName("Создание проекта")
    class ProjectCreation {

        @Test
        @DisplayName("Успешное создание проекта")
        void shouldCreateProjectSuccessfully() {
            var name = "Новый проект";
            var description = "Описание проекта";

            var project = Project.create(name, description, manager);
            assertAll(
                () -> assertEquals(name, project.name()),
                () -> assertEquals(description, project.description()),
                () -> assertEquals(manager, project.manager()),
                () -> assertTrue(project.teamMembers().containsKey(manager)),
                () -> assertInstanceOf(UserRole.Manager.class, project.teamMembers().get(manager)),
                () -> assertTrue(project.teamLeader().isEmpty()),
                () -> assertNotNull(project.createdAt())
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Создание проекта с некорректным названием должно выбрасывать исключение")
        void shouldThrowExceptionForInvalidName(String invalidName) {
            assertThrows(IllegalArgumentException.class,
                () -> Project.create(invalidName, "description", manager));
        }

        @Test
        @DisplayName("Создание проекта без менеджера должно выбрасывать исключение")
        void shouldThrowExceptionForNullManager() {
            assertThrows(IllegalArgumentException.class,
                () -> Project.create("Project", "Description", null));
        }
    }

    @Nested
    @DisplayName("Управление командой")
    class TeamManagement {

        @Test
        @DisplayName("Добавление разработчика в команду")
        void shouldAddDeveloperToTeam() {
            var project = Project.create("Project", "Description", manager);
            var developerRole = new UserRole.Developer(developer);

            var updatedProject = project.addTeamMember(developer, developerRole);

            assertAll(
                () -> assertTrue(updatedProject.teamMembers().containsKey(developer)),
                () -> assertInstanceOf(UserRole.Developer.class, updatedProject.teamMembers().get(developer)),
                () -> assertEquals(2, updatedProject.teamMembers().size())
            );
        }

        @Test
        @DisplayName("Добавление уже существующего участника должно выбрасывать исключение")
        void shouldThrowExceptionForDuplicateTeamMember() {
            var project = Project.create("Project", "Description", manager);
            var managerRole = new UserRole.Manager(manager);

            assertThrows(IllegalStateException.class,
                () -> project.addTeamMember(manager, managerRole));
        }

        @Test
        @DisplayName("Назначение тимлидера")
        void shouldAssignTeamLeader() {
            var project = Project.create("Project", "Description", manager)
                .addTeamMember(developer, new UserRole.Developer(developer));

            var updatedProject = project.assignTeamLeader(developer);

            assertTrue(updatedProject.teamLeader().isPresent());
            assertEquals(developer, updatedProject.teamLeader().get());
            assertInstanceOf(UserRole.TeamLeader.class, updatedProject.teamMembers().get(developer));
        }

        @Test
        @DisplayName("Назначение тимлида, который не участвует в проекте, должно выбрасывать исключение")
        void shouldThrowExceptionForNonTeamMemberAsLeader() {
            var project = Project.create("Project", "Description", manager);
            var outsider = new User(3L, "Сторонний пользователь", "outsider@example.com");

            assertThrows(IllegalStateException.class,
                () -> project.assignTeamLeader(outsider));
        }
    }

    @Nested
    @DisplayName("Функциональные методы")
    class FunctionalMethods {

        @Test
        @DisplayName("Получение списка разработчиков")
        void shouldReturnDevelopers() {
            var dev1 = new User(2L, "Dev1", "dev1@example.com");
            var dev2 = new User(3L, "Dev2", "dev2@example.com");
            var tester = new User(4L, "Tester", "tester@example.com");

            var project = Project.create("Project", "Description", manager)
                .addTeamMember(dev1, new UserRole.Developer(dev1))
                .addTeamMember(dev2, new UserRole.Developer(dev2))
                .addTeamMember(tester, new UserRole.Tester(tester));

            var developers = project.getDevelopers();

            assertAll(
                () -> assertEquals(2, developers.size()),
                () -> assertTrue(developers.contains(dev1)),
                () -> assertTrue(developers.contains(dev2)),
                () -> assertFalse(developers.contains(tester))
            );
        }

        @Test
        @DisplayName("Получение списка тестировщиков")
        void shouldReturnTesters() {
            var developer = new User(2L, "Developer", "dev@example.com");
            var tester1 = new User(3L, "Tester1", "tester1@example.com");
            var tester2 = new User(4L, "Tester2", "tester2@example.com");

            var project = Project.create("Project", "Description", manager)
                .addTeamMember(developer, new UserRole.Developer(developer))
                .addTeamMember(tester1, new UserRole.Tester(tester1))
                .addTeamMember(tester2, new UserRole.Tester(tester2));

            var testers = project.getTesters();

            assertAll(
                () -> assertEquals(2, testers.size()),
                () -> assertTrue(testers.contains(tester1)),
                () -> assertTrue(testers.contains(tester2)),
                () -> assertFalse(testers.contains(developer))
            );
        }

        @Test
        @DisplayName("Проверка разрешений пользователя")
        void shouldCheckUserPermissions() {
            var project = Project.create("Project", "Description", manager)
                .addTeamMember(developer, new UserRole.Developer(developer));

            assertAll(
                () -> assertTrue(project.userHasPermission(manager, UserRole.Permission.MANAGE_USERS)),
                () -> assertTrue(project.userHasPermission(developer, UserRole.Permission.EXECUTE_TICKETS)),
                () -> assertFalse(project.userHasPermission(developer, UserRole.Permission.MANAGE_USERS))
            );
        }
    }
}