package app.dto;


import app.enums.OrderStatus;
import app.util.TimeUtil;

import java.sql.Timestamp;

public record OrderOverviewDTO(
        int orderId,
        String customerFullName,
        String email,
        Timestamp customerRequestCreatedAt,
        OrderStatus orderStatus
)
{
    public String getRelativeTime()
    {
        return TimeUtil.getRelativeTime(customerRequestCreatedAt);
    }
}
