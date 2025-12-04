package app.services;

import app.entities.MaterialLine;
import app.exceptions.DatabaseException;
import app.persistence.MaterialLineMapper;

public class MaterialService implements IMaterialService
{
    private MaterialLineMapper materialLineMapper;

    public MaterialService(MaterialLineMapper materialLineMapper)
    {
        this.materialLineMapper = materialLineMapper;
    }

    @Override
    public boolean updateBillOfMaterialLineQuantity(int materialLineId, int quantity) throws DatabaseException
    {
        return materialLineMapper.updateMaterialLineQuantity(materialLineId, quantity);
    }

    @Override
    public boolean deleteBillOfMaterialLine(int materialLineId) throws DatabaseException
    {
        return materialLineMapper.deleteMaterialLine(materialLineId);
    }

    @Override
    public double getLineTotalByMaterialId(int materialLineId) throws DatabaseException
    {
        MaterialLine materialLine = materialLineMapper.getMaterialLineById(materialLineId);
        return calculateLineTotal(materialLine);
    }

    @Override
    public double getUpdatedLinePrice(int materialLineId, int quantity) throws DatabaseException
    {
        MaterialLine materialLine = materialLineMapper.getMaterialLineById(materialLineId);

    }

    private double calculateLineTotal(MaterialLine materialLine)
    {
        return materialLine.getQuantity() * materialLine.getMaterialVariant().getUnitPrice();
    }
}
