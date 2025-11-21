package app.persistence;

import app.entities.Material;
import app.entities.MaterialLine;
import app.exceptions.DatabaseException;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MaterialLineMapper
{
    private ConnectionPool connectionPool;
    private MaterialMapper materialMapper;

    public MaterialLineMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.materialMapper = new MaterialMapper(connectionPool);
    }

    public MaterialLine createMaterialLine(MaterialLine materialLine) throws DatabaseException
    {
        /*String sql = """
                INSERT INTO material_line (bom_id, material_id, quantity, line_total)
                VALUES  (?, ?, ?, ?)
                RETURNING material_line_id
                """;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, materialLine.getBomId());
            ps.setInt(2, materialLine.getMaterial().getMaterialId());
            ps.setInt(3, materialLine.getQuantity());
            ps.setDouble(4, materialLine.getLineTotal());
            // METHOD BODY NOT FINISHED!
        }
        catch (SQLException)
        {
            throw new DatabaseException("");
        }
        return null;
         */
        return null;
    }

    public MaterialLine getMaterialLineById(MaterialLine materialLine) throws DatabaseException
    {
        return null;
    }

    public boolean updateMaterialLine(MaterialLine materialLineId) throws DatabaseException
    {
        return false;
    }

    public boolean deleteMaterialLine(int materialLineId) throws DatabaseException
    {
        return false;
    }
}
