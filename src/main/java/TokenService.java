import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import fr.maif.socleapi.croupier.configuration.ConfigurationCroupier;
import fr.maif.socleapi.croupier.configuration.ConfigurationSecrets;
import fr.maif.socleapi.croupier.exceptions.CentralConfigException;
import fr.maif.socleapi.croupier.exceptions.ProfileNotFoundException;
import fr.maif.socleapi.croupier.model.*;
import fr.maif.socleapi.croupier.repository.TokenRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Service
public class TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);
    private static final String ROLES = "roles";

    private static final String EXTRANET_CHANNEL = "extranet";
    private static final String INTRANET_CHANNEL = "intranet";
    private static final String INTERNET_CHANNEL = "internet";

    private static final String BUILDKLIF = "build-klif";
    private static final String PROD = "prod";

    private static final long CONVERT_MINUTES_TO_MS = 60000;


    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ConfigurationCroupier configurationCroupier;

    @Autowired
    private ConfigurationSecrets configurationSecrets;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private RestTemplate apiRestTemplate;

    public List<TokenInfos> getTokens() {
        return tokenRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
    }

    public TokenInfos postToken(NewToken newTokenInfos, boolean isAdmin) throws ProfileNotFoundException, CentralConfigException {

        Optional<Profile> profile = profileService.findProfileByName(newTokenInfos.getProfile());
        if(profile.isEmpty()) {
            throw new ProfileNotFoundException();
        }

        Date createdDate = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(System.currentTimeMillis() + newTokenInfos.getTtl() * CONVERT_MINUTES_TO_MS);

        JWTCreator.Builder builder = JWT.create()
                .withIssuer(newTokenInfos.getToken().getIss())
                .withAudience(newTokenInfos.getToken().getAud())
                .withExpiresAt(expirationDate)
                .withIssuedAt(createdDate);

        builder.withSubject(newTokenInfos.getToken().getSub());
        builder.withClaim(ROLES, profile.get().getRoles());

        newTokenInfos
                .getToken()
                .getClaims()
                .forEach(builder::withClaim);
        String secret = getTokenSecret(newTokenInfos, profile.get(), isAdmin);
        byte[] secretBytesArray = createSecretBytesArray(secret, false, true);

        String encodeToken = builder.sign(HMAC512(secretBytesArray));
        TokenInfos tokenInfos = new TokenInfos(encodeToken, createdDate, expirationDate,
                profile.get().getChannel(),
                profile.get().getRoles(),
                newTokenInfos);
        // On sauvegarde que les tokens des environnements sensetives
        Optional<String> envirenementNameAdminOp = configurationCroupier.getSensitiveEnvs().stream()
                .filter(env -> env.equals(newTokenInfos.getStack())).findFirst();
        if (envirenementNameAdminOp.isPresent()) {
            tokenRepository.save(tokenInfos);
        }

        return tokenInfos;
    }

    private String getTokenSecret(NewToken newTokenInfos, Profile profile, boolean isAdmin) throws CentralConfigException {
        String secret = Optional.ofNullable(newTokenInfos.getToken().getSecret()).map(String::trim).orElse(null);
        if(secret == null || !isAdmin) {
            secret = getSecretFromProfile(profile, newTokenInfos.getStack());
        }
        return secret;
    }

    public List<Environment> checkTokenOnEnvironments(String token) {
        List<String> channels = Arrays.asList(EXTRANET_CHANNEL, INTRANET_CHANNEL, INTERNET_CHANNEL);

        return Stream.of(new Environment(BUILDKLIF), new Environment(PROD))
                .map(env -> {
                    List<String> stacks = getCroupierStacks(env.getName());
                    stacks
                            .forEach(stack -> {
                                Stack stackObject = new Stack(stack);
                                channels.forEach(channel -> stackObject
                                        .setState(channel, isValid(channel, stack, token)));
                                env.addStack(stackObject);
                            });
                    return env;
                })
                .collect(Collectors.toList());
    }

    private List<String> getCroupierStacks(String rancherEnv) {
        if(PROD.equals(rancherEnv)) {
            return configurationCroupier.getSensitiveEnvs();
        } else {
            return configurationCroupier.getUnsensitiveEnvs();
        }
    }

    private boolean isValid(String channel, String stack, String token) {
        try {
            String secret = getSecretFromProfile(new Profile(channel), stack);

            JWTVerifier jwtVerifier = JWT
                    .require(Algorithm.HMAC512(createSecretBytesArray(secret, false, true)))
                    .build();
            jwtVerifier.verify(token);
            return true;
        } catch (CentralConfigException e) {
            LOGGER.debug("La configuration sur le canal {} et le channel {} n'a pas pu être récupéré : {}",
                    channel, stack, e.getMessage(), e);
            return false;
        } catch(JWTVerificationException e) {
            LOGGER.debug("Token not signed with secret", e);
            return false;
        }
    }

    private static byte[] createSecretBytesArray(String key, boolean isEncodedSecret, boolean isLegacySecret) {
        byte[] bytesArray;
        if (isLegacySecret) {
            bytesArray = DatatypeConverter.parseBase64Binary(key);
        } else if (isEncodedSecret) {
            bytesArray = Base64.decodeBase64(key.getBytes());
        } else {
            bytesArray = key.getBytes();
        }
        return bytesArray;
    }

    private String getSecretFromProfile(Profile profile, String stack) throws CentralConfigException {
        Optional<String> secretOpt= Optional.ofNullable(configurationSecrets.get(stack))
                .map(canalSecrets ->canalSecrets.get(profile.getChannel()) );
        if (secretOpt.isEmpty()) {
            throw new CentralConfigException("Aucun secret n'a été trouvé pour le profil");
        }
        return secretOpt.get();
    }

    public void updateToken(TokenInfos tokenInfos) {
        tokenRepository.save(tokenInfos);
    }
}
