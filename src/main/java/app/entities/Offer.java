package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Offer
{
    private int OfferId;
    private User seller;
    private User customer;
    private Carport carport;
    private OfferDate offerDate;

}
