package rpa.record;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsageDuration {
    final LocalTime startTime;
    final LocalTime endTime;

    public UsageDuration(final LocalTime startTime, final LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public List<UsageDuration> split(final Interrupts interrupt) {
        List<UsageDuration> result = new ArrayList<>();
        if (interrupt.isValid() && isInBetween(this, interrupt)) {
            result.add(new UsageDuration(this.startTime, interrupt.startTime));
            if(!interrupt.endTime.equals(this.endTime)) {
                result.add(new UsageDuration(interrupt.endTime, this.endTime));
            }
        } else {
            result.add(this);
        }
        return result;
    }

    private static boolean isInBetween(final UsageDuration usage,
                                final Interrupts interrupt){
        return usage.startTime.isBefore(interrupt.startTime) && usage.endTime.isAfter(interrupt.startTime) &&
                usage.startTime.isBefore(interrupt.endTime) && (usage.endTime.isAfter(interrupt.endTime) || usage.endTime.equals(interrupt.endTime));
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return startTime.format(formatter) + "-" + endTime.format(formatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsageDuration that = (UsageDuration) o;
        return Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }
}
