package in.pervush.poker.configuration;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class PushConfiguration {

    @Bean
    public ApnsClient apnsClient(final PushProperties properties)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        final var apnsProperties = properties.getApns();

        return new ApnsClientBuilder()
                .setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                .setSigningKey(ApnsSigningKey.loadFromPkcs8File(
                        new File(apnsProperties.getP8KeyPath()),
                        apnsProperties.getTeamId(),
                        apnsProperties.getKeyId()
                ))
                .build();
    }
}
