package app.services;

import app.entities.Carport;
import app.entities.MaterialLine;
import app.entities.PricingDetails;
import app.exceptions.DatabaseException;
import app.exceptions.MaterialNotFoundException;

import java.util.List;

public interface IBomService
{
    public List<MaterialLine> getBillOfMaterialByCarport(Carport carport) throws DatabaseException, MaterialNotFoundException;
    public PricingDetails calculateCarportPrice(List<MaterialLine> billOfMaterial);
}
