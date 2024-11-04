import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Pinboard {
    private static final String PINBOARD_API_ENDPOINT = "https://api.pinboard.in/v1/";
    private static final SimpleDateFormat PINBOARD_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    private String token;

    public Pinboard(String token) {
        this.token = token;
    }

    public List<Bookmark> getRecentPosts(int count, Date date) throws IOException, InterruptedException, ParseException {
        Map<String, String> params = new HashMap<>();
        params.put("count", String.valueOf(count));
        params.put("fromdt", PINBOARD_DATETIME_FORMAT.format(date));

        String response = callApi("posts/recent", params);
        return parseBookmarks(response);
    }

    public List<Bookmark> getPosts(String url, boolean meta) throws IOException, InterruptedException, ParseException {
        Map<String, String> params = new HashMap<>();
        params.put("url", url);
        params.put("meta", meta ? "yes" : "no");

        String response = callApi("posts/get", params);
        return parseBookmarks(response);
    }

    private String callApi(String endpoint, Map<String, String> params) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        String urlWithParams = PINBOARD_API_ENDPOINT + endpoint + "?format=json&auth_token=" + token + "&" + toQueryString(params);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlWithParams)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("API request failed with status " + response.statusCode());
        }
        return response.body();
    }

    private String toQueryString(Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (query.length() > 0) {
                query.append("&");
            }
            query.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return query.toString();
    }

    private List<Bookmark> parseBookmarks(String response) throws IOException, ParseException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        ArrayList<Bookmark> bookmarks = new ArrayList<>();
        for (JsonNode node : root.path("posts")) {
            bookmarks.add(new Bookmark(node, token));
        }
        return bookmarks;
    }

    public static class Bookmark {
        private String description;
        private String extended;
        private String url;
        private String meta;
        private String hash;
        private boolean shared;
        private boolean toread;
        private List<String> tags;
        private Date time;
        private String token;

        public Bookmark(JsonNode payload, String token) throws ParseException {
            this.description = payload.path("description").asText();
            this.extended = payload.path("extended").asText();
            this.url = payload.path("href").asText();
            this.meta = payload.path("meta").asText();
            this.hash = payload.path("hash").asText();
            this.shared = "yes".equals(payload.path("shared").asText());
            this.toread = "yes".equals(payload.path("toread").asText());
            this.tags = Arrays.asList(payload.path("tags").asText().split(" "));
            this.time = PINBOARD_DATETIME_FORMAT.parse(payload.path("time").asText());
            this.token = token;
        }
        
        public String getDescription() {
            return description;
        }

        public String getExtended() {
            return extended;
        }

        public String getUrl() {
            return url;
        }

        public String getMeta() {
            return meta;
        }

        public String getHash() {
            return hash;
        }

        public boolean isShared() {
            return shared;
        }

        public boolean isToread() {
            return toread;
        }

        public List<String> getTags() {
            return tags;
        }

        public Date getTime() {
            return time;
        }
        
        public void save() throws IOException, InterruptedException {
            Map<String, String> params = new HashMap<>();
            params.put("url", this.url);
            params.put("description", this.description);
            params.put("extended", this.extended);
            params.put("tags", String.join(" ", this.tags));
            params.put("shared", this.shared ? "yes" : "no");
            params.put("toread", this.toread ? "yes" : "no");
            params.put("dt", PINBOARD_DATETIME_FORMAT.format(this.time));

            new Pinboard(this.token).callApi("posts/add", params);
        }

        public void delete() throws IOException, InterruptedException {
            Map<String, String> params = new HashMap<>();
            params.put("url", this.url);

            new Pinboard(this.token).callApi("posts/delete", params);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Bookmark bookmark = (Bookmark) obj;
            return Objects.equals(hash, bookmark.hash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hash);
        }

        @Override
        public String toString() {
            return "Bookmark{" +
                    "description='" + description + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
