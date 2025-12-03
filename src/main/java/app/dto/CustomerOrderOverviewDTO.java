package app.dto;

import app.enums.OrderStatus;
import app.util.TimeUtil;

import java.sql.Timestamp;

public record CustomerOrderOverviewDTO(
        int orderId,
        Timestamp customerRequestCreatedAt,
        Timestamp offerValidDays,
        Timestamp orderFinalazedDate,
        OrderStatus orderStatus
)
{
    public String getRelativeTime()
    {
        return TimeUtil.getRelativeTime(customerRequestCreatedAt);
    }
}
