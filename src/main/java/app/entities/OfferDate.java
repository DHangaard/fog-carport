package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class OfferDate
{
    private Timestamp customerRequestCreatedAt;
    private Timestamp createdAt;
    private Timestamp expirationDate;

    public boolean isExpired()
    {
        return false;
    }

    public void calculateExpirationDate()
    {
        //TODO implement body
    }
}
