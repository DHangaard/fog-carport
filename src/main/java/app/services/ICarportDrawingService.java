package app.services;

import app.entities.Carport;

public interface ICarportDrawingService
{
    public Svg getCarportTopSvgView(Carport carport);
    public Svg getCarportSideSvgView(Carport carport);
}
