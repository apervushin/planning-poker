package in.pervush.poker.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sendinblue.ApiException;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailTo;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendinblueService {

    private final TransactionalEmailsApi transactionalEmailsApi;

    private static SendSmtpEmailTo buildSendSmtpEmailTo(final String email, final String name) {
        final var smtpEmailTo = new SendSmtpEmailTo();
        return smtpEmailTo.email(email).name(name);
    }

    private static SendSmtpEmail buildSendSmtpEmail(final String email, final String name,
                                                    final Map<String, String> params, final long templateId) {
        final var smtpEmail = new SendSmtpEmail();
        smtpEmail.setTemplateId(templateId);
        return smtpEmail.addToItem(buildSendSmtpEmailTo(email, name)).params(params);
    }

    public void sendEmail(final String email, final String name, final Map<String, String> params, final long templateId) {
        try {
            transactionalEmailsApi.sendTransacEmail(buildSendSmtpEmail(email, name, params, templateId));
        } catch (final ApiException e) {
            throw new RuntimeException(e);
        }
    }
}
