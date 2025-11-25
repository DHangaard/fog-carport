package app.controllers;

import app.services.ICarportDrawingService;
import app.services.IOrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class OrderController
{
        private IOrderService orderService;
        private ICarportDrawingService carportDrawingService;

        public OrderController(IOrderService orderService, ICarportDrawingService carportDrawingService)
        {
            this.orderService = orderService;
            this.carportDrawingService = carportDrawingService;
        }

        public void addRoutes(Javalin app)
        {
            app.get("/carport-drawing", ctx -> showCarportDrawing(ctx));
        }

    private void showCarportDrawing(Context ctx)
    {
    }
}
