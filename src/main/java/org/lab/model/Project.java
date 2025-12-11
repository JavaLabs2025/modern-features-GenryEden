package org.lab.model;

import org.lab.user.User;
import org.lab.user.UserRole;

import java.time.LocalDateTime;
import java.util.*;

public record Project(
        Long id,
        String name,
        String description,
        Map<User, UserRole> teamMembers,
        Optional<User> teamLeader,
        User manager,
        Set<Long> milestoneIds,
        Set<Long> bugReportIds,
        LocalDateTime createdAt
) {
    public Project {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название проекта не может быть пустым");
        }
        if (manager == null) {
            throw new IllegalArgumentException("У проекта должен быть менеджер");
        }
        if (teamMembers == null) {
            teamMembers = Map.of();
        }
        if (teamLeader == null) {
            teamLeader = Optional.empty();
        }
        if (milestoneIds == null) {
            milestoneIds = Set.of();
        }
        if (bugReportIds == null) {
            bugReportIds = Set.of();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static Project create(String name, String description, User manager) {
        if (manager == null) {
            throw new IllegalArgumentException("У проекта должен быть менеджер");
        }

        return new Project(
                null,
                name,
                description,
                Map.of(manager, new UserRole.Manager(manager)),
                Optional.empty(),
                manager,
                Set.of(),
                Set.of(),
                LocalDateTime.now()
        );
    }

    public Project addTeamMember(User user, UserRole role) {
        if (teamMembers.containsKey(user)) {
            throw new IllegalStateException(STR."Пользователь \{user.name()} уже участвует в проекте");
        }

        var newTeamMembers = new HashMap<>(teamMembers);
        newTeamMembers.put(user, role);

        return new Project(id, name, description, newTeamMembers, teamLeader, manager,
                milestoneIds, bugReportIds, createdAt);
    }

    public Project assignTeamLeader(User user) {
        if (!teamMembers.containsKey(user)) {
            throw new IllegalStateException("Тимлидер должен быть участником проекта");
        }

        var newTeamMembers = new HashMap<>(teamMembers);
        newTeamMembers.put(user, new UserRole.TeamLeader(user));

        return new Project(id, name, description, newTeamMembers, Optional.of(user), manager,
                milestoneIds, bugReportIds, createdAt);
    }

    public Project addMilestone(Long milestoneId) {
        var newMilestoneIds = new HashSet<>(milestoneIds);
        newMilestoneIds.add(milestoneId);

        return new Project(id, name, description, teamMembers, teamLeader, manager,
                newMilestoneIds, bugReportIds, createdAt);
    }

    public Project addBugReport(Long bugReportId) {
        var newBugReportIds = new HashSet<>(bugReportIds);
        newBugReportIds.add(bugReportId);

        return new Project(id, name, description, teamMembers, teamLeader, manager,
                milestoneIds, newBugReportIds, createdAt);
    }

    public List<User> getDevelopers() {
        return teamMembers.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof UserRole.Developer)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<User> getTesters() {
        return teamMembers.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof UserRole.Tester)
                .map(Map.Entry::getKey)
                .toList();
    }

    public boolean userHasPermission(User user, UserRole.Permission permission) {
        var role = teamMembers.get(user);
        return role != null && role.hasPermission(permission);
    }

    public long getActiveMilestonesCount(List<Milestone> allMilestones) {
        return allMilestones.stream()
                .filter(milestone -> milestoneIds.contains(milestone.id()))
                .filter(Milestone::isActive)
                .count();
    }
}