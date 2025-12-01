package app.controllers;

import app.entities.Carport;
import app.entities.Shed;
import app.enums.RoofType;
import app.enums.ShedPlacement;
import app.services.svg.CarportSvgSide;
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
            app.post("/show-carport", ctx -> showCarportDrawing(ctx));
        }

    private void showCarportDrawing(Context ctx)
    {
        Locale.setDefault(new Locale("US"));

        String length = ctx.formParam("length");
        String width = ctx.formParam("width");

        int carportLength = Integer.parseInt(length);
        int carportWidth = Integer.parseInt(width);

        Carport carport = new Carport(0,carportLength, carportWidth, RoofType.FLAT,new Shed(0,180,530, ShedPlacement.FULL_WIDTH));
        CarportSvgTop carportSvgTop = carportDrawingService.getCarportTopSvgView(carport);
        CarportSvgSide carportSvgSide = carportDrawingService.getCarportSideSvgView(carport);
        ctx.attribute("carportTop", carportSvgTop.toString());
        ctx.attribute("carportSide", carportSvgSide);
        ctx.render("carport-drawing");
    }
}
