package app.entities;

import app.enums.RoofType;
import lombok.Data;

@Data
public class Carport
{
    private int carportId;
    private int length;
    private int width;
    private RoofType roofType;
    private Shed shed;
    private boolean hasShed;

    public Carport(int carportId, int length, int width, RoofType roofType, Shed shed)
    {
        this.carportId = carportId;
        this.length = length;
        this.width = width;
        this.roofType = roofType;
        this.shed = shed;
        this.hasShed = shed == null;
    }
}



