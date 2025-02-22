import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UtilsService {

    public boolean isValidJson(String responseText) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(responseText);
            return true;
        } catch (Exception e) {
            log.error("Invalid JSON response: " + responseText);
            return false;
        }
    }

    public String preprocessGPTResponse(String responseText) {
        if (responseText.startsWith("```json")) {
            responseText = responseText.substring(7).trim();
        } else if (responseText.startsWith("```")) {
            responseText = responseText.substring(3).trim();
        }

        if (responseText.endsWith("```")) {
            responseText = responseText.substring(0, responseText.length() - 3).trim();
        }

        return responseText.replace("`", "").trim();
    }

    public List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(text.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }

    public Map<String, Object> retryParsingWithSmallerChunks(String responseText) {
        log.info("Retrying parsing with smaller chunks...");

        // Split the response text into chunks of 1000 characters
        int chunkSize = 1000;  // You can adjust this size based on the typical response size
        List<String> chunks = splitTextIntoChunks(responseText, chunkSize);
        List<Map<String, Object>> fileSuggestions = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        // Process each chunk individually
        for (String chunk : chunks) {
            try {
                // Try parsing each chunk individually
                Map<String, Object> chunkSuggestions = mapper.readValue(chunk, new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> files = (List<Map<String, Object>>) chunkSuggestions.get("files");

                // If the chunk has valid files, add them to fileSuggestions
                if (files != null && !files.isEmpty()) {
                    for (Map<String, Object> fileObj : files) {
                        fileSuggestions.add(fileObj);
                    }
                }
            } catch (JsonParseException e) {
                log.warn("Skipping invalid chunk due to JSON parsing error: " + e.getMessage());
            } catch (Exception e) {
                log.error("Error processing chunk: " + e.getMessage());
            }
        }

        // Return the combined suggestions from all valid chunks
        if (!fileSuggestions.isEmpty()) {
            return Map.of("fileSuggestions", fileSuggestions);
        } else {
            return Map.of("status", "error", "message", "Unable to parse the GPT response, even in smaller chunks.");
        }
    }

    public String extractCodeFromDiff(String diff) {
        StringBuilder codeBuilder = new StringBuilder();
        String[] diffLines = diff.split("\n");

        // Skip lines that start with diff markers or non-code elements
        for (String line : diffLines) {
            // Skip file mode, diff metadata, and line indicators
            if (!line.startsWith("diff") && !line.startsWith("index") && !line.startsWith("---") && !line.startsWith("+++")
                    && !line.startsWith("@@") && !line.contains("new file mode")) {

                // Strip out leading '+' or '-' which indicate additions/deletions in the diff
                if (line.startsWith("+") || line.startsWith("-")) {
                    codeBuilder.append(line.substring(1)).append("\n");
                } else {
                    codeBuilder.append(line).append("\n");
                }
            }
        }

        return codeBuilder.toString().trim();
    }

    public String addLineNumbersToCode(String code) {
        StringBuilder numberedCode = new StringBuilder();
        String[] lines = code.split("\n");

        // Start the numbering from 0 and adjust it by adding the correct index
        for (int i = 0; i < lines.length; i++) {
            numberedCode.append(i + 1).append(": ").append(lines[i]).append("\n");
        }

        return numberedCode.toString().trim();
    }
}
