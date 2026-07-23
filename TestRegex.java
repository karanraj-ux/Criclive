import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONArray;

public class TestRegex {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://www.cricbuzz.com/cricket-match/live-scores");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();
            String html = sb.toString();
            
            Pattern p = Pattern.compile("\\\\\"typeMatches\\\\\":(.*?)\\]\\}\\]\\}");
            Matcher m = p.matcher(html);
            if (m.find()) {
                String jsonStr = m.group(1) + "]}]";
                jsonStr = jsonStr.replace("\\\"", "\"");
                try {
                    JSONArray arr = new JSONArray(jsonStr);
                    System.out.println("JSON Parsed Successfully! Length: " + arr.length());
                } catch(Exception e) {
                    System.out.println("JSON Parse Error: " + e.getMessage());
                    System.out.println("Beginning: " + jsonStr.substring(0, Math.min(100, jsonStr.length())));
                    System.out.println("Ending: " + jsonStr.substring(Math.max(0, jsonStr.length() - 100)));
                }
            } else {
                System.out.println("NOT FOUND");
            }
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
