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

        }
        return new CarportSvgTop(carport.getWidth(), carport.getLength());
    }

    @Override
    public Svg getCarportSideSvgView(Carport carport)
    {
        return null;
    }
}
