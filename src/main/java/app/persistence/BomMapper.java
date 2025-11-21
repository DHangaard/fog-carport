package app.persistence;

import app.entities.BillOfMaterials;

public class BomMapper
{
    private ConnectionPool connectionPool;
    private MaterialLineMapper materialLineMapper;


    public BomMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.materialLineMapper = new MaterialMapper(connectionPool);
    }

    public BillOfMaterials createBillOfMaterials(BillOfMaterials billOfMaterials)
    {
        //billOfMaterials.getMaterialLines().forEach(materialLine -> materialLine);
        return null;
    }

    public BillOfMaterials getBillOfMaterialsById(int offerId)
    {
        return null;
    }

    public boolean updateBillOfMaterials(BillOfMaterials billOfMaterials)
    {
        return false;
    }

    public boolean deleteBillOfMaterials(int bomId)
    {
        return false;
    }



}
