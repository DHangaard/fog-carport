package app.services;

import app.entities.Carport;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;

public class CarportDrawingService implements ICarportDrawingService
{
    @Override
    public CarportSvgTop getCarportTopSvgView(Carport carport)
    {
        if(carport == null)
        {
            throw new IllegalArgumentException("Carport mål skal være udfyldt");
        }
        return new CarportSvgTop(carport);
    }

    @Override
    public CarportSvgSide getCarportSideSvgView(Carport carport)
    {
        if(carport == null)
        {
            throw new IllegalArgumentException("Carport mål skal være udfyldt");
        }
        return new CarportSvgSide(carport);
    }
}
