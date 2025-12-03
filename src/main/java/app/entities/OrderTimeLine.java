package app.entities;

import app.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

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

    public void calculateExpirationDate()
    {
        //TODO implement body
    }

    public String getRelativeTime() {
        return TimeUtil.getRelativeTime(customerRequestCreatedAt);
    }
}
