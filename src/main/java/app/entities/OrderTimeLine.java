package app.entities;

import app.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderTimeLine
{
    private Timestamp customerRequestCreatedAt;
    private Timestamp createdAt;
    private Integer offerValidDays;

    public boolean isExpired()
    {
        return false;
    }

    public String getCreatedAtFormatted()
    {
        LocalDate createdAtFormatted = createdAt.toLocalDateTime().toLocalDate();
        return formatDate(createdAtFormatted);
    }

    public String getExpirationDateFormatted()
    {
        LocalDate expiration = calculateExpirationDate();
        if (expiration != null)
        {
            return formatDate(expiration);
        }
        return null;
    }

    private LocalDate calculateExpirationDate()
    {
        if (createdAt != null || offerValidDays != null)
        {
            return createdAt.toLocalDateTime().toLocalDate().plusDays(offerValidDays);
        }
        return null;
    }

    public String getRelativeTime()
    {
        return TimeUtil.getRelativeTime(customerRequestCreatedAt);

    }

    private String formatDate(LocalDate localDate)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy", new Locale("da", "DK"));
        return localDate.format(formatter);
    }
}
