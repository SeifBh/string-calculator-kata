import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
public class HeadersRestTemplateService {

    @Value("${github.token}")
    private String githubToken;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    public HttpHeaders createGitHubHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    public HttpHeaders createOpenAiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

}
