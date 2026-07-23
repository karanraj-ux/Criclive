import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;

public class TestExtract {
    public static void main(String[] args) throws Exception {
        String html = new String(Files.readAllBytes(Paths.get("cricbuzz.html")));
        String searchStr = "\\\"typeMatches\\\":[";
        int idx = html.indexOf(searchStr);
        if (idx == -1) {
            System.out.println("Not found");
            return;
        }
        
        int startIdx = idx + searchStr.length() - 1; // start at '['
        int brackets = 0;
        int endIdx = -1;
        
        boolean inString = false;
        boolean escape = false;
        
        for (int i = startIdx; i < html.length(); i++) {
            char c = html.charAt(i);
            
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            
            if (!inString) {
                if (c == '[') brackets++;
                else if (c == ']') brackets--;
                
                if (brackets == 0) {
                    endIdx = i;
                    break;
                }
            }
        }
        
        if (endIdx != -1) {
            String jsonStr = html.substring(startIdx, endIdx + 1);
            jsonStr = jsonStr.replace("\\\"", "\"");
            jsonStr = jsonStr.replace("\\\\", "\\"); // handle double escapes if any
            System.out.println("Extracted length: " + jsonStr.length());
            try {
                JSONArray arr = new JSONArray(jsonStr);
                System.out.println("Parsed successfully! Length: " + arr.length());
            } catch(Exception e) {
                System.out.println("Parse error: " + e.getMessage());
            }
        } else {
            System.out.println("Could not find matching bracket");
        }
    }
}
