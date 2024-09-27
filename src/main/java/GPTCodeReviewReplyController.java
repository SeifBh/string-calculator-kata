import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class GPTCodeReviewReplyController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.temperature}")
    private double temperature;

    @PostMapping("/review-code-comment-reply")
    public ResponseEntity<Map<String, String>> submitCodeReviewReply(@RequestBody ReplyRequest replyRequest) {
        // Extract lineNumber, comment, and the original code
        int lineNumber = replyRequest.getLineNumber();
        String comment = replyRequest.getComment();
        String code = replyRequest.getCode();

        // Process the reply (log or store the reply)
        String resultMessage = "Reply for line " + lineNumber + " received: " + comment;

        // Call GPT for a concise response to the comment
        String gptResponse = getGPTReply(lineNumber, code, comment);

        // Return both the original result message and GPT's response
        Map<String, String> response = new HashMap<>();
        response.put("userReply", resultMessage);
        response.put("gptReply", gptResponse);

        return ResponseEntity.ok(response);
    }

    // Call GPT API to provide a reply strictly based on the user's comment
    private String getGPTReply(int lineNumber, String code, String comment) {
        try {
            // Build GPT prompt: Focus on the user's comment only
            String prompt = String.format(
                    "The user commented on line %d: \"%s\".\n"
                            + "Please respond directly and concisely to the user's comment based on the context of the code.",
                    lineNumber, comment
            );

            // Build the request body for GPT API
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", new JsonNode[] {
                    mapper.createObjectNode().put("role", "user").put("content", prompt)
            });
            requestBody.put("temperature", temperature);

            // Make the API call to GPT
            String response = restTemplate.postForObject(apiUrl, requestBody, String.class);

            // Extract GPT's response
            JsonNode responseJson = mapper.readTree(response);
            String gptReply = responseJson.get("choices").get(0).get("message").get("content").asText();

            return gptReply;
        } catch (Exception e) {
            return "Error retrieving GPT response: " + e.getMessage();
        }
    }

    // Request body class
    public static class ReplyRequest {
        private int lineNumber;
        private String comment;
        private String code;

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}
