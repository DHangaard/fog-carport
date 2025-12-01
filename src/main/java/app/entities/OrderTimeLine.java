package app.entities;

import app.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class OrderTimeLine
{
    private Timestamp customerRequestCreatedAt;
    private Timestamp createdAt;
    private int offerValidDays;

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
