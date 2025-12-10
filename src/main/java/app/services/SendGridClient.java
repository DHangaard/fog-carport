package app.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.io.IOException;
import java.util.Map;

public class SendGridClient
{
    private final String apiKey;
    private final String fromEmail = "info@fog.jensify.dk";
    private final String fromName = "Johannes Fog Byggemarked";
    private final String category = "carportapp";

    public SendGridClient(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public boolean sendMail(String sendEmailTo, String templateId, Map<String,Object> dynamicData) {
        Email from = new Email(fromEmail);
        from.setName(fromName);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(sendEmailTo));

        if(dynamicData != null)
        {
            dynamicData.forEach((key, value) -> personalization.addDynamicTemplateData(key, value));
        }

        mail.addPersonalization(personalization);
        mail.addCategory(category);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try
        {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            return response.getStatusCode() == 202;
        }
        catch (IOException ex)
        {
            System.out.println("Error sending mail: " + ex.getMessage());
            return false;
        }
    }
}
