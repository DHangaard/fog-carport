package app.services;

public class SendGridClient
{
    private final String apiKey;
    private final String fromEmail = "johannes.fog.xl.byg@gmail.com";
    private final String fromName = "Johannes Fog Byggemarked";
    private final String category = "carportapp";

    public SendGridClient(String apiKey)
    {
        this.apiKey = apiKey;
    }
}
