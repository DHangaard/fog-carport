package app.services;

import app.dto.UserDTO;
import app.entities.OrderDetail;

import java.util.HashMap;
import java.util.Map;

public class SendGridEmailService implements IEmailService
{
    private SendGridClient sendGridClient;
    private final String REQUEST_CONFIRMATION_TEMPLATE = "d-2f92a4eacaea449b99167a35094140c5";
    private final String OFFER_READY_TEMPLATE = "d-a16e0ee9ed34411ca0acee887bfdb22a";
    private final String ORDER_CONFIRMATION_TEMPLATE = "d-ae86cc47271143398d6064a7e7de3d4f";

    public SendGridEmailService()
    {
        String apiKey = System.getenv("SENDGRID_API_KEY");
        this.sendGridClient = new SendGridClient(apiKey);
    }

    @Override
    public boolean sendRequestConfirmation(UserDTO user)
    {
        Map<String,Object> dynamicData = buildDynamicUserData(user);

        if(!hasDataToSend(dynamicData))
        {
            return false;
        }

        return sendGridClient.sendMail(user.email(), REQUEST_CONFIRMATION_TEMPLATE, dynamicData);
    }

    @Override
    public boolean sendOfferReady(UserDTO user)
    {
        Map<String,Object> dynamicData = buildDynamicUserData(user);

        if(!hasDataToSend(dynamicData))
        {
            return false;
        }

        return sendGridClient.sendMail(user.email(), OFFER_READY_TEMPLATE, dynamicData);
    }

    @Override
    public boolean sendOrderConfirmation(UserDTO user, OrderDetail orderDetail)
    {
        Map<String,Object> dynamicData = buildDynamicOrderAndUserData(user, orderDetail);

        if(!hasDataToSend(dynamicData))
        {
            return false;
        }

        return sendGridClient.sendMail(user.email(), ORDER_CONFIRMATION_TEMPLATE, dynamicData);
    }

    private Map<String, Object> buildDynamicUserData(UserDTO user)
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

    private Map<String, Object> buildDynamicOrderAndUserData(UserDTO user, OrderDetail orderDetail)
    {
        Map<String, Object> dynamicData = buildDynamicUserData(user);

        if (orderDetail == null)
        {
            return dynamicData;
        }

        dynamicData.put("orderId", orderDetail.getOrderId());

        if (orderDetail.getCarport() != null)
        {
            dynamicData.put("carportLength", orderDetail.getCarport().getLength());
            dynamicData.put("carportWidth", orderDetail.getCarport().getWidth());
        }

        if (orderDetail.getPricingDetails() != null)
        {
            dynamicData.put("pricingWithoutVat", orderDetail.getPricingDetails().getPriceWithoutVat());
            dynamicData.put("totalPrice", orderDetail.getPricingDetails().getTotalPrice());
        }

        return dynamicData;
    }

    private boolean hasDataToSend(Map<String, Object> dynamicData)
    {
        return dynamicData != null && !dynamicData.isEmpty();
    }
}
