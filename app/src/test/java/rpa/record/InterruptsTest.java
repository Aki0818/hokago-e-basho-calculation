package rpa.record;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class InterruptsTest {

    static Stream<Arguments> dateProvider() {
        return Stream.of(
                arguments("15:00", "17:00", "18:00", "15:00", "17:00", true),
                arguments("15:00", "17:00", null, "15:00", "17:00", true),
                arguments("15:00", null, "18:00", "15:00", "18:00", true),
                arguments(null, "17:00", "18:00", null, "17:00", false),
                arguments("15:00", null, null, "15:00", null, false),
                arguments(null, "17:00", null, null, "17:00", false),
                arguments(null, null, "18:00", null, null, false),
                arguments(null, null, null, null, null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("dateProvider")
    void build(String start, String end, String durationEnd, String expectedStart, String expectedEnd, boolean isValid) {
        var startTime = start == null ? null : LocalTime.parse(start);
        var endTime = end == null ? null : LocalTime.parse(end);
        var durationEndTime = durationEnd == null ? null : LocalTime.parse(durationEnd);

        var actual = Interrupts.build(startTime, endTime, durationEndTime);


        assertEquals(actual.startTime, expectedStart == null ? null : LocalTime.parse(expectedStart));
        assertEquals(actual.endTime, expectedEnd == null ? null : LocalTime.parse(expectedEnd));
        assertEquals(actual.isValid(), isValid);
    }
}
