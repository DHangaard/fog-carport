package app.services;

import app.exceptions.DatabaseException;

public interface IMaterialService
{
    public boolean updateBillOfMaterialLineQuantity(int materialLineId, int quantity) throws DatabaseException;
    public boolean deleteBillOfMaterialLine(int materialLineId) throws DatabaseException;
    public double calculateLineTotal(int materialLineId) throws DatabaseException;
}
