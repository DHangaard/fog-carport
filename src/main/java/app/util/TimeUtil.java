package app.util;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil
{
    public static String getRelativeTime(Timestamp timestamp)
    {
        if (timestamp == null)
        {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime requestOrderTime = timestamp.toLocalDateTime();
        Duration duration = Duration.between(requestOrderTime, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1)
        {
            return "Lige nu";
        }

        if (minutes < 60)
        {
            return minutes + " min siden";
        }

        if (hours < 24)
        {
            return hours + " timer siden";
        }

        if (days < 7)
        {
            return days + " dage siden";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        return requestOrderTime.format(formatter);
    }
}
