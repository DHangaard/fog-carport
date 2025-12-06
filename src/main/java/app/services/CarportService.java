package app.services;

import app.entities.Carport;
import app.entities.Shed;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;
import app.persistence.CarportMapper;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import app.util.ValidationUtil;

public class CarportService implements ICarportService
{
    private CarportMapper carportMapper;
    private static final int SHED_SIDE_MARGIN = 35;

    public CarportService(CarportMapper carportMapper)
    {
        this.carportMapper = carportMapper;
    }

    @Override
    public void validateCarport(Carport carport)
    {
        int carportWidth = carport.getWidth();
        int carportLength = carport.getLength();

        ValidationUtil.validateCarportDimensions(carportWidth, carportLength);

        Shed shed = carport.getShed();
        if (shed != null)
        {
            ValidationUtil.validateShedDimensions(carportWidth, carportLength, shed, SHED_SIDE_MARGIN);
        }
    }

    @Override
    public Carport getCarportByCarportId(int carportId) throws DatabaseException
    {
        return carportMapper.getCarportById(carportId);
    }

    @Override
    public Shed createShedWithPlacement(int carportWidth, int shedWidth, int shedLenght)
    {
        ShedPlacement shedPlacement = getShedPlacement(carportWidth, shedWidth);
        return new Shed(0,shedLenght, shedWidth, shedPlacement);
    }

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

    public ShedPlacement getShedPlacement(int carportWidth, int shedWidth)
    {
        int shedSides = 2;
        int maxFullWidthShedWidth = carportWidth - (shedSides * SHED_SIDE_MARGIN);

        if (shedWidth >= maxFullWidthShedWidth)
        {
            return ShedPlacement.FULL_WIDTH;
        }

        return ShedPlacement.LEFT;
    }
}
