package app.controllers;

import app.services.IOrderService;
import io.javalin.Javalin;

public class OrderController
{
        private IOrderService orderService;

        public OrderController(IOrderService orderService)
        {
            this.orderService = orderService;
        }

        public void addRoutes(Javalin app)
        {

        }
}
