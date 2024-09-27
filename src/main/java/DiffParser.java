import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffParser {

    public static List<Map<String, Object>> parseDiff(String diff) {
        List<Map<String, Object>> lineSuggestions = new ArrayList<>();
        String[] lines = diff.split("\n");

        String currentFile = null;
        int lineNumber = 0;

        for (String line : lines) {
            if (line.startsWith("diff --git")) {
                currentFile = extractFilePath(line);
            } else if (line.startsWith("@@")) {
                lineNumber = extractLineNumberFromHunk(line);
            } else if (line.startsWith("+") && !line.startsWith("+++")) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("line", lineNumber);
                suggestion.put("comment", "Auto-generated review comment.");
                suggestion.put("path", currentFile);
                lineSuggestions.add(suggestion);
                lineNumber++;
            } else if (!line.startsWith("-") && !line.startsWith(" ")) {
                lineNumber++;
            }
        }
        return lineSuggestions;
    }

    private static String extractFilePath(String diffLine) {
        String[] parts = diffLine.split(" ");
        return parts[2].substring(2); // Extract the file path
    }

    private static int extractLineNumberFromHunk(String hunkLine) {
        Pattern pattern = Pattern.compile("\\+([0-9]+)");
        Matcher matcher = pattern.matcher(hunkLine);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
