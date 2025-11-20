package app.entities;

import app.enums.RoofType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Carport
{
    private int carportId;
    private int length;
    private int width;
    private RoofType roofType;
    private Shed shed;
    private boolean hasShed;
}

