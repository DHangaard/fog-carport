package app.controllers;

import app.entities.Carport;
import app.enums.RoofType;
import app.services.svg.CarportSvgTop;
import app.services.ICarportDrawingService;
import app.services.IOrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Locale;

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
        Locale.setDefault(new Locale("US"));

        String length = ctx.formParam("length");
        String width = ctx.formParam("width");

        int carportLength = Integer.parseInt(length);
        int carportWidth = Integer.parseInt(width);

        Carport carport = new Carport(0,carportLength, carportWidth, RoofType.FLAT,null);
        CarportSvgTop carportSvgTop = carportDrawingService.getCarportTopSvgView(carport);
        ctx.attribute("carportTop", carportSvgTop.toString());
        ctx.render("order");
    }
}
