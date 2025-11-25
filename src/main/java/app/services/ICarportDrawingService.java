package app.services;

import app.entities.Carport;
import app.services.svg.CarportSvgTop;
import app.services.svg.Svg;

public interface ICarportDrawingService
{
    public CarportSvgTop getCarportTopSvgView(Carport carport);
    public Svg getCarportSideSvgView(Carport carport);
}
