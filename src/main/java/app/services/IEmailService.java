package app.services;

import app.dto.UserDTO;
import app.entities.OrderDetail;

public interface IEmailService
{
    boolean sendRequestConfirmation(UserDTO user);
    boolean sendOfferReady(UserDTO user);
    boolean sendOrderConfirmation(UserDTO user, OrderDetail orderDetail);
}
