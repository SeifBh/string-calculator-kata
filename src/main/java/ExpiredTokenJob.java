import fr.maif.socleapi.croupier.model.TokenInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpiredTokenJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredTokenJob.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MailService mailService;

    final String helloBlabla;

    private static final long MONTH_MS = 2628000000L;

    @Scheduled(fixedRate = 604800000)
    public void checkExpirationDate() {
        List<TokenInfos> expiredTokens = tokenService
                .getTokens()
                .stream()
                .filter(token -> token.getExpirationDate().getTime() < (new Date(System.currentTimeMillis()).getTime() + MONTH_MS))
                .collect(Collectors.toList());

        expiredTokens
                .stream()
                .filter(token -> !token.isMailSent())
                .forEach(token -> {
                    try {
                        mailService.send(
                                token.getId(),
                                new SimpleDateFormat("dd/MM/yyyy").format(token.getExpirationDate()),
                                token.getEmails());
                        token.setMailSent(true);
                        tokenService.updateToken(token);
                    } catch(MailException e) {
                        LOGGER.info("Le mail n'a pas pu partir", e);
                    }
                });
    }
}
