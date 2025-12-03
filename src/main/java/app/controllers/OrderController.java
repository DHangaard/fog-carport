package app.controllers;

import app.dto.UserDTO;
import app.enums.Role;
import app.services.IOrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class OrderController
{
        private IOrderService orderService;

        public OrderController(IOrderService orderService)
        {
            this.orderService = orderService;
        }

        public void addRoutes(Javalin app)
        {
            app.get("/offers", ctx -> showOfferOverview(ctx));
            app.get("/orders", ctx -> showOrderOverview(ctx));

        }

    private void showOrderOverview(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}
    }

    private void showOfferOverview(Context ctx)
    {
        if(!userIsAdmin(ctx)){return;}
    }

    private boolean userIsAdmin(Context ctx)
    {
        UserDTO userDTO = ctx.sessionAttribute("currentUser");

        if(userDTO == null)
        {
            ctx.sessionAttribute("errorMessage", "Du skal logge ind for at tilgå denne side");
            ctx.redirect("/login");
            return false;
        }

        return userDTO.role().equals(Role.SALESREP);
    }
}
