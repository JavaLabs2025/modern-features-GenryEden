package org.lab.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты переходов статусов")
class StatusTransitionTest {

    @TestFactory
    @DisplayName("Динамические тесты переходов статусов вехи")
    Stream<DynamicTest> milestoneStatusTransitions() {
        return Stream.of(
            DynamicTest.dynamicTest("Open -> Active (разрешено)",
                () -> {
                    var open = new MilestoneStatus.Open();
                    var active = new MilestoneStatus.Active();
                    assertTrue(open.canTransitionTo(active));
                }),

            DynamicTest.dynamicTest("Active -> Closed (разрешено)",
                () -> {
                    var active = new MilestoneStatus.Active();
                    var closed = new MilestoneStatus.Closed();
                    assertTrue(active.canTransitionTo(closed));
                }),

            DynamicTest.dynamicTest("Open -> Closed (запрещено)",
                () -> {
                    var open = new MilestoneStatus.Open();
                    var closed = new MilestoneStatus.Closed();
                    assertFalse(open.canTransitionTo(closed));
                }),

            DynamicTest.dynamicTest("Closed -> любой статус (запрещено)",
                () -> {
                    var closed = new MilestoneStatus.Closed();
                    var open = new MilestoneStatus.Open();
                    var active = new MilestoneStatus.Active();

                    assertAll(
                        () -> assertFalse(closed.canTransitionTo(open)),
                        () -> assertFalse(closed.canTransitionTo(active)),
                        () -> assertFalse(closed.canTransitionTo(closed))
                    );
                })
        );
    }

    @ParameterizedTest
    @MethodSource("ticketStatusTransitionsProvider")
    @DisplayName("Параметризованные тесты переходов статусов тикета")
    void ticketStatusTransitions(TicketStatus from, TicketStatus to, boolean expected) {
        assertEquals(expected, from.canTransitionTo(to));
    }

    private static Stream<Arguments> ticketStatusTransitionsProvider() {
        return Stream.of(
            Arguments.of(new TicketStatus.New(), new TicketStatus.Accepted(), true),
            Arguments.of(new TicketStatus.Accepted(), new TicketStatus.InProgress(), true),
            Arguments.of(new TicketStatus.InProgress(), new TicketStatus.Completed(), true),

            Arguments.of(new TicketStatus.New(), new TicketStatus.InProgress(), false),
            Arguments.of(new TicketStatus.New(), new TicketStatus.Completed(), false),
            Arguments.of(new TicketStatus.Accepted(), new TicketStatus.New(), false),
            Arguments.of(new TicketStatus.Completed(), new TicketStatus.InProgress(), false)
        );
    }

    @Test
    @DisplayName("Переходы статусов баг-репорта")
    void bugReportStatusTransitions() {
        var newStatus = new BugReportStatus.New();
        var fixed = new BugReportStatus.Fixed();
        var tested = new BugReportStatus.Tested();
        var closed = new BugReportStatus.Closed();

        assertAll(
            () -> assertTrue(newStatus.canTransitionTo(fixed)),
            () -> assertTrue(fixed.canTransitionTo(tested)),
            () -> assertTrue(tested.canTransitionTo(closed)),

            () -> assertFalse(newStatus.canTransitionTo(tested)),
            () -> assertFalse(newStatus.canTransitionTo(closed)),
            () -> assertFalse(fixed.canTransitionTo(closed)),
            () -> assertFalse(closed.canTransitionTo(newStatus))
        );
    }

    @Test
    @DisplayName("Получение человекочитаемых названий статусов")
    void statusDisplayNames() {
        assertAll(
            // Milestone статусы
            () -> assertEquals("Открыт", new MilestoneStatus.Open().getDisplayName()),
            () -> assertEquals("Активен", new MilestoneStatus.Active().getDisplayName()),
            () -> assertEquals("Закрыт", new MilestoneStatus.Closed().getDisplayName()),

            // Ticket статусы
            () -> assertEquals("Новый", new TicketStatus.New().getDisplayName()),
            () -> assertEquals("Принятый", new TicketStatus.Accepted().getDisplayName()),
            () -> assertEquals("В процессе выполнения", new TicketStatus.InProgress().getDisplayName()),
            () -> assertEquals("Выполнен", new TicketStatus.Completed().getDisplayName()),

            // BugReport статусы
            () -> assertEquals("Новый", new BugReportStatus.New().getDisplayName()),
            () -> assertEquals("Исправленный", new BugReportStatus.Fixed().getDisplayName()),
            () -> assertEquals("Протестированный", new BugReportStatus.Tested().getDisplayName()),
            () -> assertEquals("Закрытый", new BugReportStatus.Closed().getDisplayName())
        );
    }
}