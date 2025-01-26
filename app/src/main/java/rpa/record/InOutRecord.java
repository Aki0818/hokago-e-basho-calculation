package rpa.record;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public class InOutRecord implements Comparable {
    final String name;
    final String date;
    final List<UsageDuration> usageHours;

    private boolean hasError = false;

    public InOutRecord(String name, String date, List<UsageDuration> usageHours) {
        this.name = name;
        this.date = date;
        this.usageHours = usageHours;
    }

    public void setError(){
        this.hasError = true;
    }

    public boolean isError(){
        return this.hasError;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String toStringForDetail(){

        String warning = "";
        if (this.isError()) {
            warning = "!!";
        }

        String detail = "";
        for(UsageDuration hours : usageHours){
            detail = detail + hours.toString() + " / " ;
        }

        return String.format("%-14s : %-56s( %3s/%3s min) %s",
                date,
                detail,
                getBeforeMin(),
                getExtendMin(),
                warning
        );
    }


    @Override
    public int compareTo(Object o) {
        InOutRecord oo = (InOutRecord) o;
        if (this.getName().equals(oo.getName())) {
            return this.getDate().compareTo(oo.getDate());
        } else {
            return this.getName().compareTo(oo.getName());
        }
    }

    public int getRoundedBeforeMin() {
        LocalTime before = LocalTime.of(8, 30);
        return usageHours.stream().
                filter(h -> h.startTime.isBefore(before))
                .map(h -> {
                    LocalTime endTime = getEarly(h.endTime, before);
                    return Duration.between(h.startTime, endTime);
                })
                .map(Duration::toMinutes)
                .map(m -> ((m + 14) / 15) * 15)
                .mapToInt(Long::intValue)
                .sum();
    }

    public int getBeforeMin() {
        LocalTime before = LocalTime.of(8, 30);
        return usageHours.stream().
                filter(h -> h.startTime.isBefore(before))
                .map(h -> {
                    LocalTime endTime = getEarly(h.endTime, before);
                    return Duration.between(h.startTime, endTime);
                })
                .map(Duration::toMinutes)
                .mapToInt(Long::intValue)
                .sum();
    }

    public int getRoundedExtendMin() {
        LocalTime after = LocalTime.of(18, 29);
        return usageHours.stream().
                filter(h -> h.endTime.isAfter(after))
                .map(h -> {
                    LocalTime startTime = getLatey(h.startTime, after);
                    return Duration.between(startTime, h.endTime);
                })
                .map(Duration::toMinutes)
                .map(m -> ((m + 14) / 15) * 15)
                .mapToInt(Long::intValue)
                .sum();
    }

    public int getExtendMin() {
        LocalTime after = LocalTime.of(18, 29);
        return usageHours.stream().
                filter(h -> h.endTime.isAfter(after))
                .map(h -> {
                    LocalTime startTime = getLatey(h.startTime, after);
                    return Duration.between(startTime, h.endTime);
                })
                .map(Duration::toMinutes)
                .mapToInt(Long::intValue)
                .sum();
    }

    private LocalTime getEarly(LocalTime time1, LocalTime time2) {
        return time1.isBefore(time2) ? time1 : time2;
    }

    private LocalTime getLatey(LocalTime time1, LocalTime time2) {
        return time1.isAfter(time2) ? time1 : time2;
    }

}


