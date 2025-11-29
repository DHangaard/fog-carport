package app.services;

import app.dto.UserDTO;

public class SendGridEmailService implements IEmailService
{
    private SendGridClient sendGridClient;
    private final String REQUEST_CONFIRMATION_TEMPLATE = "d-ae86cc47271143398d6064a7e7de3d4f";
    private final String OFFER_READY_TEMPLATE = "d-OFFER_READY";
    private final String ORDER_CONFIRMATION_TEMPLATE = "d-ORDER_CONFIRMATION";

    public SendGridEmailService()
    {
        String apiKey = System.getenv("SENDGRID_API_KEY");
        this.sendGridClient = new SendGridClient(apiKey);
    }

    @Override
    public boolean sendRequestConfirmation(UserDTO user)
    {
        return false;
    }

    @Override
    public boolean sendOfferReady(UserDTO user)
    {
        return false;
    }

    @Override
    public boolean sendOrderConfirmation(UserDTO user)
    {
        return false;
    }
}
