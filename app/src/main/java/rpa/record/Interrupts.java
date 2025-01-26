package rpa.record;

import java.time.LocalTime;

public class Interrupts {
    final LocalTime startTime;
    final LocalTime endTime;

    public static Interrupts build(final LocalTime startTime, final LocalTime endTime, final LocalTime usageHoursEndTime) {
        if (startTime != null && endTime == null) {
            return new Interrupts(startTime, usageHoursEndTime);
        }
        return new Interrupts(startTime, endTime);
    }

    Interrupts(final LocalTime startTime, final LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isValid() {
        return startTime != null && endTime != null;
    }

}
