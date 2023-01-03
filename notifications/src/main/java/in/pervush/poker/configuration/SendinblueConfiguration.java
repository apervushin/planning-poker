package in.pervush.poker.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;

@Configuration
public class SendinblueConfiguration {

    @Bean
    public TransactionalEmailsApi transactionalEmailsApi(@Value("${sendinblue.api_key}") final String apiKey) {
        final var defaultClient = sendinblue.Configuration.getDefaultApiClient();
        final var apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        return new TransactionalEmailsApi(defaultClient);
    }
}
