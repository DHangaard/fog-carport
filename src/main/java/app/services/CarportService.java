package app.services;

import app.entities.Carport;
import app.entities.Shed;

public class CarportService implements ICarportService
{
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
}
