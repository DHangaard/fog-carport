package app.util;

import app.dto.UserDTO;
import app.enums.OrderStatus;
import app.exceptions.DatabaseException;
import app.services.IOrderService;
import io.javalin.http.Context;

public class BeforeHandlersUtil
{
    public static void addBagdeCount(Context ctx, IOrderService orderService)
    {
        UserDTO currentUser = ctx.sessionAttribute("currentUser");

        if (currentUser != null && currentUser.role().name(). equals("SALESREP"))
        {
            Boolean badgeNeedsUpdate = ctx.sessionAttribute("badgeNeedsUpdate");
            Integer cachedBadgeCount = ctx.sessionAttribute("requestCount");

            if (badgeNeedsUpdate == null || badgeNeedsUpdate || cachedBadgeCount == null)
            {
                try
                {
                    int requestCount = orderService.getTotalNumberOfOrdersByStatus(OrderStatus.PENDING);
                    ctx.sessionAttribute("requestCount", requestCount);
                    ctx.sessionAttribute("bagdeNeedsUpdate", false);
                    ctx.attribute("requestCount", requestCount);
                }
                catch (DatabaseException e)
                {
                    ctx.attribute("requestCount", 0);
                }
            }
            else
            {
                ctx.attribute("requestCount", cachedBadgeCount);
            }
        }
    }
}
