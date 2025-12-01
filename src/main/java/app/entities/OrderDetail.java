package app.entities;

import app.enums.OrderStatus;
import app.services.svg.CarportSvgSide;
import app.services.svg.CarportSvgTop;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderDetail
{
    private int orderId;
    private User seller;
    private User customer;
    private Carport carport;
    private OrderTimeLine orderTimeLine;
    private List<MaterialLine> materialLines;
    private CarportSvgTop carportSvgTop;
    private CarportSvgSide carportSvgSide;
    private String customerComment;
    private PricingDetails pricingDetails;
    private OrderStatus orderStatus;

    public void addMaterialLine(MaterialLine materialLine)
    {
        if(materialLine != null)
        {
            materialLines.add(materialLine);
        }
    }

    public void removeMaterialLine(MaterialLine materialLine)
    {
        if(materialLine != null)
        {
            materialLines.remove(materialLine);
        }
    }
}
