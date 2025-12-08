package app.services;

import app.entities.MaterialLine;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import app.persistence.MaterialLineMapper;
import app.persistence.MaterialVariantMapper;

import java.util.List;

public class MaterialService implements IMaterialService
{
    private MaterialLineMapper materialLineMapper;
    private MaterialVariantMapper materialVariantMapper;

    public MaterialService(MaterialLineMapper materialLineMapper, MaterialVariantMapper materialVariantMapper)
    {
        this.materialLineMapper = materialLineMapper;
        this.materialVariantMapper = materialVariantMapper;
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

    @Override
    public List<MaterialVariant> searchMaterials(String searchType, String query) throws DatabaseException
    {
        if(searchType == null || searchType.isEmpty() || query == null || query.isEmpty())
        {
            throw new IllegalArgumentException("Dit søge input mangler en type eller søgning prøv igen");
        }

        return switch (searchType.toLowerCase())
        {
            case "id" -> materialVariantMapper.searchById(parseQuery(query));
            case "name" -> materialVariantMapper.searchByName(query);
            case "category" -> materialVariantMapper.searchByCategory(query.toUpperCase());
            case "type" -> materialVariantMapper.searchByType(query.toUpperCase());
            default -> List.of();
        };
    }

    private int parseQuery(String query)
    {
        try
        {
            return Integer.parseInt(query);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Kun hel tal i søgning efter id");
        }
    }

    public List<MaterialVariant> getMaterialVariantsByType(MaterialType materialType) throws DatabaseException
    {
        return null;
    }
    public List<MaterialVariant> getMaterialVariantsByName(String name) throws DatabaseException
    {
        return null;
    }
    public List<MaterialVariant> getMaterialVariantsById(int materialVariantId) throws DatabaseException
    {
        return null;
    }
    public List<MaterialVariant> getMaterialVariantsByCategory(MaterialCategory category) throws DatabaseException
    {
        return null;
    }

    private double calculateLineTotal(MaterialLine materialLine)
    {
        return materialLine.getQuantity() * materialLine.getMaterialVariant().getUnitPrice();
    }
}
