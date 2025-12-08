package app.services;

import app.entities.MaterialLine;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;
import app.persistence.MaterialLineMapper;
import app.persistence.MaterialVariantMapper;

import java.util.List;
import java.util.stream.Collectors;

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
            case "id" ->
            {
                if (!query.matches("\\d+"))
                {
                    throw new IllegalArgumentException("ID skal være et tal.");
                }
                yield materialVariantMapper.searchById(Integer.parseInt(query));
            }
            case "name" -> getMaterialVariantsByName(query);
            case "category" -> getMaterialVariantsByCategory(query);
            case "type" -> getMaterialVariantsByType(query);
            default -> List.of();
        };
    }

    @Override
    public MaterialVariant getMaterialVariantById(int materialVariantId) throws DatabaseException
    {
        return materialVariantMapper.getVariantWithMaterialById(materialVariantId);
    }

    private List<MaterialVariant> getMaterialVariantsByType(String type) throws DatabaseException
    {
        return materialVariantMapper.getAllMaterialVariants().stream()
                .filter(materialVariant -> materialVariant.getMaterial().getType().getDisplayName().toLowerCase().contains(type.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<MaterialVariant> getMaterialVariantsByName(String name) throws DatabaseException
    {
        return materialVariantMapper.getAllMaterialVariants().stream()
                .filter(materialVariant -> materialVariant.getMaterial().getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<MaterialVariant> getMaterialVariantsByCategory(String category) throws DatabaseException
    {
        return materialVariantMapper.getAllMaterialVariants().stream()
                .filter(materialVariant -> materialVariant.getMaterial().getCategory().getDisplayCategory().toLowerCase().contains(category.toLowerCase()))
                .collect(Collectors.toList());
    }

    private double calculateLineTotal(MaterialLine materialLine)
    {
        return materialLine.getQuantity() * materialLine.getMaterialVariant().getUnitPrice();
    }
}
