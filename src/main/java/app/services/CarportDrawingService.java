package app.services;

import app.entities.Carport;
import app.services.svg.CarportSvgTop;
import app.services.svg.Svg;

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
    public Svg getCarportSideSvgView(Carport carport)
    {
        return null;
    }
}
