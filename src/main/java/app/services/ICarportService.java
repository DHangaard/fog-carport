package app.services;

import app.entities.Carport;
import app.entities.Shed;
import app.exceptions.DatabaseException;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;

public interface ICarportService
{
    public void validateCarport(Carport carport);
    public Carport getCarportByCarportId(int carportId) throws DatabaseException;
    public Shed createShedWithPlacement(int carportWidth, int shedWidth, int shedLenght);
    public CarportSvgTop getCarportTopSvgView(Carport carport);
    public CarportSvgSide getCarportSideSvgView(Carport carport);
}
