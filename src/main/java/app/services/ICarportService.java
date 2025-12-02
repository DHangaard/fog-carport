package app.services;

import app.entities.Carport;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;

public interface ICarportService
{
    public void validateCarport(Carport carport);
    public CarportSvgTop getCarportTopSvgView(Carport carport);
    public CarportSvgSide getCarportSideSvgView(Carport carport);
}
