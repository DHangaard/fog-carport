package app.persistence;

public class OfferMapper
{
    private ConnectionPool connectionPool;
    private BomMapper bomMapper;
    private CarportMapper carportMapper;

    public OfferMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
        this.bomMapper = new BomMapper(connectionPool);
        this.carportMapper = new CarportMapper(connectionPool);
    }

    public
}
