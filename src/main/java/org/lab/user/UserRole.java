package org.lab.user;

import java.util.Set;

public sealed interface UserRole
        permits UserRole.Manager, UserRole.TeamLeader, UserRole.Developer, UserRole.Tester {

    record Manager(User user) implements UserRole {
        public Set<Permission> getPermissions() {
            return Set.of(
                    Permission.MANAGE_USERS,
                    Permission.MANAGE_MILESTONES,
                    Permission.MANAGE_TICKETS,
                    Permission.CREATE_PROJECT,
                    Permission.VIEW_PROJECT
            );
        }
    }

    record TeamLeader(User user) implements UserRole {
        public Set<Permission> getPermissions() {
            return Set.of(
                    Permission.MANAGE_TICKETS,
                    Permission.EXECUTE_TICKETS,
                    Permission.VIEW_PROJECT
            );
        }
    }

    record Developer(User user) implements UserRole {
        public Set<Permission> getPermissions() {
            return Set.of(
                    Permission.EXECUTE_TICKETS,
                    Permission.CREATE_BUG_REPORTS,
                    Permission.FIX_BUG_REPORTS,
                    Permission.VIEW_PROJECT
            );
        }
    }

    record Tester(User user) implements UserRole {
        public Set<Permission> getPermissions() {
            return Set.of(
                    Permission.TEST_PROJECT,
                    Permission.CREATE_BUG_REPORTS,
                    Permission.VERIFY_BUG_FIXES,
                    Permission.VIEW_PROJECT
            );
        }
    }

    default User getUser() {
        return switch (this) {
            case Manager(var user) -> user;
            case TeamLeader(var user) -> user;
            case Developer(var user) -> user;
            case Tester(var user) -> user;
        };
    }

    default String getRoleName() {
        return switch (this) {
            case Manager manager -> "Менеджер";
            case TeamLeader teamLeader -> "Тимлидер";
            case Developer developer -> "Разработчик";
            case Tester tester -> "Тестировщик";
        };
    }

    default boolean hasPermission(Permission permission) {
        return switch (this) {
            case Manager manager -> manager.getPermissions().contains(permission);
            case TeamLeader teamLeader -> teamLeader.getPermissions().contains(permission);
            case Developer developer -> developer.getPermissions().contains(permission);
            case Tester tester -> tester.getPermissions().contains(permission);
        };
    }

    enum Permission {
        MANAGE_USERS,
        MANAGE_MILESTONES,
        MANAGE_TICKETS,
        EXECUTE_TICKETS,
        CREATE_PROJECT,
        VIEW_PROJECT,
        CREATE_BUG_REPORTS,
        FIX_BUG_REPORTS,
        TEST_PROJECT,
        VERIFY_BUG_FIXES
    }
}