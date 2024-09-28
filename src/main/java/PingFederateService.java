import fr.maif.commons.resttemplate.MaifRestTemplateContantes;
import fr.maif.socleapi.croupier.configuration.PingFederateConfiguration;
import fr.maif.socleapi.croupier.exceptions.MissingIdTokenException;
import fr.maif.socleapi.croupier.model.PingFederateTokenData;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Service
public class PingFederateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingFederateService.class);

    @Autowired
    private PingFederateConfiguration configuration;

    @Autowired
    @Qualifier(MaifRestTemplateContantes.REST_TEMPLATE_BEAN)
    private RestTemplate restTemplate;

    public Optional<String> findAuthUrlByEnvironment(String name) {
        return configuration.getEnvironments().entrySet().stream()
                .filter(stringIdpEnvironmentInfosEntry -> stringIdpEnvironmentInfosEntry.getValue().getEnvs().stream()
                        .anyMatch(name::equals))
                .findFirst().map(Map.Entry::getValue).map(PingFederateConfiguration.IdpEnvironmentInfos::getAuthUrl);
    }

    public String formatClientId(String environmentName) {
        return String.format(
                configuration.getClientId(),
                environmentName.replace("api-", "")
        );
    }


    public PingFederateTokenData authorize(String environment, String code, String url) throws MissingIdTokenException {

        ResponseEntity<PingFederateTokenData> res = getAuthorization(environment, code, url);

        if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
            if (res.getBody().getIdToken() == null) {
                LOGGER.error("Id token null");
                throw new MissingIdTokenException();
            }
        } else {
            LOGGER.error("Erreur lors de la récupération des tokens avec le code http {}: {}", res.getStatusCode(), res.getBody());
            throw new MissingIdTokenException();
        }

        return res.getBody();
    }

    private ResponseEntity<PingFederateTokenData> getAuthorization(String environment, String code, String url) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", buildAuthHeader(environment, configuration.getClientId()));

        MultiValueMap<String, String> fields = new LinkedMultiValueMap<>();
        fields.add("grant_type", "authorization_code");
        fields.add("code", code);
        fields.add("redirect_uri",  url);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(fields, headers);

        Optional<String> tokenUrl = findTokenUrlByEnvironment(environment);

        return tokenUrl.map(tokenUrlValue ->
                restTemplate.exchange(
                        tokenUrlValue,
                        HttpMethod.POST,
                        entity,
                        PingFederateTokenData.class)).orElse(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    private Optional<String> findTokenUrlByEnvironment(String name) {
        return configuration.getEnvironments().entrySet().stream()
                .filter(stringIdpEnvironmentInfosEntry -> stringIdpEnvironmentInfosEntry.getValue().getEnvs().stream()
                        .anyMatch(name::equals))
                .findFirst().map(Map.Entry::getValue).map(PingFederateConfiguration.IdpEnvironmentInfos::getTokenUrl);
    }

    private static String buildAuthHeader(String environment, String authString) {
        return "Basic " + Base64.encodeBase64String(
                String
                        .format(authString, environment.replace("api-", ""))
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}
