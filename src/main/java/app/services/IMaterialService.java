package app.services;

import app.entities.Material;
import app.entities.MaterialVariant;
import app.enums.MaterialCategory;
import app.enums.MaterialType;
import app.exceptions.DatabaseException;

import java.util.List;

public interface IMaterialService
{
    public boolean updateBillOfMaterialLineQuantity(int materialLineId, int quantity) throws DatabaseException;
    public boolean deleteBillOfMaterialLine(int materialLineId) throws DatabaseException;
    public double getLineTotalByMaterialId(int materialLineId) throws DatabaseException;
    public double calculateLinePriceDifference(int materialLineId, int quantity) throws DatabaseException;
    public boolean deleteMaterialVariant(int materialVariantId) throws DatabaseException;
    public boolean updateMaterialVariant(MaterialVariant variant) throws DatabaseException;
    public MaterialVariant createMaterialVariant(MaterialVariant variant) throws DatabaseException;
    public List<MaterialVariant> getMaterialVariantsByType(MaterialType materialType) throws DatabaseException;
    public List<MaterialVariant> getMaterialVariantsByName(String name) throws DatabaseException;
    public List<MaterialVariant> getMaterialVariantsById(int materialVariantId) throws DatabaseException;
    public List<MaterialVariant> getMaterialVariantsByCategory(MaterialCategory category) throws DatabaseException;
}
