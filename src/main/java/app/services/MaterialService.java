package app.services;

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
}
