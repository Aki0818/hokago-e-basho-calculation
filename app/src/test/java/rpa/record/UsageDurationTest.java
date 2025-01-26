package rpa.record;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class UsageDurationTest {


    static Stream<Arguments> dateProvider() {
        return Stream.of(
                arguments("15:00", "17:00",
                        new Interrupts(LocalTime.parse("15:30"), LocalTime.parse("16:30")),
                        List.of(
                            new UsageDuration(LocalTime.parse("15:00"),LocalTime.parse("15:30")),
                            new UsageDuration(LocalTime.parse("16:30"),LocalTime.parse("17:00"))
                        )
                ),
                arguments("15:00", "17:00",
                        new Interrupts(LocalTime.parse("15:30"), LocalTime.parse("17:00")),
                        List.of(
                                new UsageDuration(LocalTime.parse("15:00"),LocalTime.parse("15:30"))
                        )
                ),
                arguments("15:00", "17:00",
                        new Interrupts(LocalTime.parse("15:30"), null),
                        List.of(
                                new UsageDuration(LocalTime.parse("15:00"),LocalTime.parse("17:00"))
                        )
                ),
                arguments("15:00", "17:00",
                        new Interrupts(LocalTime.parse("17:00"), LocalTime.parse("17:01")),
                        List.of(
                                new UsageDuration(LocalTime.parse("15:00"),LocalTime.parse("17:00"))
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("dateProvider")
    void split(String start, String end, Interrupts interrupts, List<UsageDuration> expected) {
        var startTime = start == null ? null : LocalTime.parse(start);
        var endTime = end == null ? null : LocalTime.parse(end);

        var sut = new UsageDuration(startTime, endTime);

        var actual = sut.split(interrupts);

        assertIterableEquals(actual, expected);
    }

}
