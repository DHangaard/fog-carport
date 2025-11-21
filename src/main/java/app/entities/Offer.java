package app.entities;

import app.enums.OfferStatus;
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
    private BillOfMaterials billOfMaterials;
    private String customerComment;
    private OfferStatus offerStatus;
}
