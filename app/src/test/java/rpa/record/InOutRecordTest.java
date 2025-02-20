package rpa.record;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class InOutRecordTest {
    @Test
    public void createInstance() {
        var time = new UsageDuration(
                LocalTime.of(15, 0),
                LocalTime.of(17, 30)
        );

        InOutRecord sut = new InOutRecord(
                "name",
                "2024-05-01",
                List.of(time)
        );
        assertNotNull(sut);
        assertEquals("name", sut.name);
        assertEquals("2024-05-01", sut.date);
        assertEquals(LocalTime.of(15, 0), sut.usageHours.getFirst().startTime);
        assertEquals(LocalTime.of(17, 30), sut.usageHours.getFirst().endTime);
    }


    @Test
    void toStringForDetail() {
        var time = new UsageDuration(
                LocalTime.of(15, 0),
                LocalTime.of(17, 30)
        );

        InOutRecord sut = new InOutRecord(
                "name",
                "2024-05-01",
                List.of(time)
        );

        assertEquals(
                "2024-05-01     : 15:00-17:30 /                                           (   0/  0 min) ",
                sut.toStringForDetail()
        );
    }

    static Stream<Arguments> dateProvider() {
        return Stream.of(
                arguments("15:00:00", "17:00:00", 0, 0, 0, 0),
                arguments("15:00:00", "18:30:00", 0, 0, 0, 0),
                arguments("15:00:00", "18:30:01", 0, 1, 0, 15),
                arguments("15:00:00", "18:45:00", 0, 15, 0, 15),
                arguments("15:00:00", "18:45:01", 0, 16, 0, 30),
                arguments("18:45:00", "19:00:00", 0, 15, 0, 15),
                arguments("08:29:59", "17:00:00", 1, 0, 15, 0),
                arguments("08:14:59", "17:00:00", 16, 0, 30, 0),
                arguments("08:15:00", "17:00:00", 15, 0, 15, 0),
                arguments("08:15:01", "17:00:00", 15, 0, 15, 0),
                arguments("08:01:00", "08:11:00", 10, 0, 15, 0),
                arguments("08:00:00", "19:00:00", 30, 30, 30, 30),
                arguments("08:30:00", "18:29:59", 0, 0, 0, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("dateProvider")
    void checkExtended(String start, String end, int expectedBefore, int expectedAfter, int expectedRoundedBefore, int expectedRoundedAfter) {
        var time = new UsageDuration(
                LocalTime.parse(start),
                LocalTime.parse(end)
        );

        InOutRecord sut = new InOutRecord(
                "name",
                "2024-05-01",
                List.of(time)
        );

        assertEquals(expectedBefore, sut.getBeforeMin());
        assertEquals(expectedAfter, sut.getExtendMin());
        assertEquals(expectedRoundedBefore, sut.getRoundedBeforeMin());
        assertEquals(expectedRoundedAfter, sut.getRoundedExtendMin());
    }

    static Stream<Arguments> suspendedData() {
        return Stream.of(
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("15:00:00"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("16:00:00"),
                                LocalTime.parse("17:00:00")
                        )
                ), 0, 0, 0, 0),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("15:00:00"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("16:00:00"),
                                LocalTime.parse("18:00:00")
                        )
                ), 0, 0, 0, 0),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("15:00:00"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("16:00:00"),
                                LocalTime.parse("18:30:01")
                        )
                ), 0, 1, 0, 15),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("15:00:00"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("16:00:00"),
                                LocalTime.parse("18:45:00")
                        )
                ), 0, 15, 0, 15),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("15:00:00"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("16:00:00"),
                                LocalTime.parse("18:45:01")
                        )
                ), 0, 16, 0, 30),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("15:00:00"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("18:45:00"),
                                LocalTime.parse("19:00:00")
                        )
                ), 0, 15, 0, 15),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("15:00:00"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("18:45:00"),
                                LocalTime.parse("18:45:01")
                        )
                ), 0, 1, 0, 15),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("08:29:59"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("17:45:00"),
                                LocalTime.parse("18:00:00")
                        )
                ), 1, 0, 15, 0),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("08:14:59"),
                                LocalTime.parse("15:30:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("17:45:00"),
                                LocalTime.parse("18:00:00")
                        )
                ), 16, 0, 30, 0),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("08:00:00"),
                                LocalTime.parse("08:00:01")
                        ),
                        new UsageDuration(
                                LocalTime.parse("08:15:00"),
                                LocalTime.parse("18:00:00")
                        )
                ), 16, 0, 30, 0),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("08:00:00"),
                                LocalTime.parse("08:00:01")
                        ),
                        new UsageDuration(
                                LocalTime.parse("08:15:00"),
                                LocalTime.parse("18:00:00")
                        ),
                        new UsageDuration(
                                LocalTime.parse("18:42:00"),
                                LocalTime.parse("18:42:20")
                        )

                ), 16, 1, 30, 15),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("08:00:00"),
                                LocalTime.parse("08:00:01")
                        ),
                        new UsageDuration(
                                LocalTime.parse("08:15:00"),
                                LocalTime.parse("08:15:58")
                        ),
                        new UsageDuration(
                                LocalTime.parse("18:42:00"),
                                LocalTime.parse("18:42:20")
                        ),
                        new UsageDuration(
                                LocalTime.parse("18:45:00"),
                                LocalTime.parse("18:45:39")
                        )

                ), 1, 1, 15, 15),
                arguments(List.of(
                        new UsageDuration(
                                LocalTime.parse("08:00:00"),
                                LocalTime.parse("08:00:02")
                        ),
                        new UsageDuration(
                                LocalTime.parse("08:15:00"),
                                LocalTime.parse("08:15:59")
                        ),
                        new UsageDuration(
                                LocalTime.parse("18:42:00"),
                                LocalTime.parse("18:42:20")
                        ),
                        new UsageDuration(
                                LocalTime.parse("18:45:00"),
                                LocalTime.parse("18:45:41")
                        )

                ), 2, 2, 15, 15)
        );
    }

    @ParameterizedTest
    @MethodSource("suspendedData")
    void checkExtendedWithSuspend(List<UsageDuration> time, int expectedBefore, int expectedAfter, int expectedRoundedBefore, int expectedRoundedAfter) {
        InOutRecord sut = new InOutRecord(
                "name",
                "2024-05-01",
                time
        );

        assertEquals(expectedBefore, sut.getBeforeMin());
        assertEquals(expectedAfter, sut.getExtendMin());
        assertEquals(expectedRoundedBefore, sut.getRoundedBeforeMin());
        assertEquals(expectedRoundedAfter, sut.getRoundedExtendMin());
    }

}