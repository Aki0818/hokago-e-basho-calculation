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
                "2024-05-01     : 15:00-17:30 /                                           (   0 min) ",
                sut.toStringForDetail()
        );
    }

    static Stream<Arguments> dateProvider() {
        return Stream.of(
                arguments("15:00", "17:00", 0, 0, 0, 0),
                arguments("15:00", "18:30", 0, 1, 0, 15),
                arguments("15:00", "18:44", 0, 15, 0, 15),
                arguments("15:00", "18:45", 0, 16, 0, 30),
                arguments("18:45", "19:00", 0, 15, 0, 15),
                arguments("08:29", "17:00", 1, 0, 15, 0),
                arguments("08:15", "17:00", 15, 0, 15, 0),
                arguments("08:14", "17:00", 16, 0, 30, 0),
                arguments("08:01", "08:11", 10, 0, 15, 0),
                arguments("08:00", "19:00", 30, 31, 30, 45),
                arguments("08:30", "18:29", 0, 0, 0, 0)
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

}