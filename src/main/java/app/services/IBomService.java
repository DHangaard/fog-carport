package app.services;

import app.entities.Carport;
import app.entities.MaterialLine;
import app.entities.PricingDetails;
import app.exceptions.DatabaseException;

import java.util.List;

public interface IBomService
{
    public List<MaterialLine> getBillOfMaterialByCarport(Carport carport) throws DatabaseException;

    public PricingDetails calculateCarportPrice(List<MaterialLine> billOfMaterial);
}
