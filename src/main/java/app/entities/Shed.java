package app.entities;

import app.enums.ShedPlacement;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Shed
{
    private int shedId;
    private int length;
    private int width;
    private ShedPlacement shedPlacement;
}
