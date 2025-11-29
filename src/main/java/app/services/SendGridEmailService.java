package app.services;

import app.dto.UserDTO;

import java.util.HashMap;
import java.util.Map;

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
        Map<String,Object> dynamicData = buildDynamicData(user);

        if(!hasDataToSend(dynamicData))
        {
            return false;
        }

        return sendGridClient.sendMail(user.email(), REQUEST_CONFIRMATION_TEMPLATE, dynamicData);
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

    private Map<String, Object> buildDynamicData(UserDTO user)
    {
        Map<String, Object> dynamicData = new HashMap<>();

        if (user == null)
        {
            return dynamicData;
        }

        if (user.firstName() != null && !user.firstName().isEmpty())
        {
            dynamicData.put("firstName", user.firstName());
        }

        if (user.lastName() != null && !user.lastName().isEmpty())

        {
            dynamicData.put("lastName", user.lastName());
        }

        return dynamicData;
    }

    private boolean hasDataToSend(Map<String, Object> dynamicData)
    {
        return dynamicData != null && !dynamicData.isEmpty();
    }
}
