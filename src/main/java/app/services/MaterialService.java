package app.services;

import app.entities.MaterialLine;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import app.persistence.MaterialLineMapper;

import java.util.List;

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
    public double calculateLinePriceDifference(int materialLineId, int quantity) throws DatabaseException
    {
        MaterialLine materialLine = materialLineMapper.getMaterialLineById(materialLineId);
        double oldMaterialLineTotal = calculateLineTotal(materialLine);
        double newMaterialLineTotal = materialLine.getMaterialVariant().getUnitPrice() * quantity;

        return newMaterialLineTotal - oldMaterialLineTotal;
    }

    @Override
    public List<MaterialVariant> getMaterialVariantsBySearchQuery(String searchQuery) throws DatabaseException
    {
        if(searchQuery == null)
        {
            throw new IllegalArgumentException("Venligst indtast et søge ord");
        }

        switch (searchQuery)
        {
            case 
        }
    }

    @Override
    public boolean deleteMaterialVariant(int materialVariantId) throws DatabaseException
    {
        return false;
    }

    @Override
    public boolean updateMaterialVariant(MaterialVariant variant) throws DatabaseException
    {
        return false;
    }

    @Override
    public MaterialVariant createMaterialVariant(MaterialVariant variant) throws DatabaseException
    {
        return null;
    }

    private List<MaterialVariant> getMaterialVariantsByType(MaterialType materialType) throws DatabaseException
    {
        return null;
    }
    private List<MaterialVariant> getMaterialVariantsByName(String name) throws DatabaseException
    {
        return null;
    }
    private List<MaterialVariant> getMaterialVariantsById(int materialVariantId) throws DatabaseException
    {
        return null;
    }
    private List<MaterialVariant> getMaterialVariantsByCategory(MaterialCategory category) throws DatabaseException
    {
        return null;
    }

    private double calculateLineTotal(MaterialLine materialLine)
    {
        return materialLine.getQuantity() * materialLine.getMaterialVariant().getUnitPrice();
    }
}
