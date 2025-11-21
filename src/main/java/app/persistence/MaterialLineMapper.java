package app.persistence;

import app.entities.Material;
import app.exceptions.DatabaseException;

public class MaterialLineMapper
{
    private ConnectionPool connectionPool;
    private MaterialMapper materialMapper;

    public MaterialLineMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.materialMapper = new MaterialMapper(connectionPool);
    }

    public MaterialLine createMaterialLine(int bomId, int materialId, int quantity, double lineTotal) throws DatabaseException
    {
        
    }


}
