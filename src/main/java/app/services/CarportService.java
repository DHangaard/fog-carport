package app.services;

import app.entities.Carport;
import app.entities.Shed;
import app.enums.ShedPlacement;
import app.exceptions.DatabaseException;
import app.persistence.CarportMapper;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;

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

        if (carportWidth < 240 || carportWidth > 600)
        {
            throw new IllegalArgumentException("Carport bredde skal være mellem 240 og 600 cm");
        }

        if (carportLength < 240 || carportLength > 780)
        {
            throw new IllegalArgumentException("Carport længde skal være mellem 240 og 780 cm");
        }


        Shed shed = carport.getShed();
        if (shed != null)
        {
            if (shed.getWidth() <= 0 || shed.getLength() <= 0)
            {
                throw new IllegalArgumentException("Skuret skal have både bredde og længde");
            }

            if (shed.getWidth() > carportWidth)
            {
                throw new IllegalArgumentException("Skurets bredde må ikke være større end carportens bredde");
            }

            if (shed.getLength() > carportLength)
            {
                throw new IllegalArgumentException("Skurets længde må ikke være større end carportens længde");
            }
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
        int maxFullWidthShedWidth = carportWidth - (2 * SHED_SIDE_MARGIN);

        if (shedWidth >= maxFullWidthShedWidth)
        {
            return ShedPlacement.FULL_WIDTH;
        }

        return ShedPlacement.LEFT;
    }
}
