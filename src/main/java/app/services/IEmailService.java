package app.services;

import app.dto.UserDTO;

public interface IEmailService
{
    boolean sendRequestConfirmation(UserDTO user);
    boolean sendOfferReady(UserDTO user);
    boolean sendOrderConfirmation(UserDTO user);
}
