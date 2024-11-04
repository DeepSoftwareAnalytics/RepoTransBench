import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamUser extends SteamAPI {

    private boolean visible;
    private String username;
    private String profileUrl;
    private String avatar;
    private List<Map<String, Object>> gamesDict;

    public SteamUser(String steamId, String apiKey) {
        super(steamId, apiKey);
        getUserInfo();
        gamesDict = null;
    }

    private void getUserInfo() {
        String url = String.format("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=%s&steamids=%s", apiKey, steamId);
        JSONObject jsonData = new JSONObject(getJson(url));
        this.rawJson = jsonData;
        this.visible = false;
        
        JSONArray players = jsonData.getJSONObject("response").getJSONArray("players");
        if (players.length() == 0) {
            throw new RuntimeException("Error loading profile");
        } else {
            JSONObject player = players.getJSONObject(0);
            if (player.getInt("communityvisibilitystate") == 3) {
                this.visible = true;
            }
            this.username = player.getString("personaname");
            this.profileUrl = player.getString("profileurl");
            this.avatar = player.getString("avatarfull");
        }
    }

    public List<Map<String, Object>> getGames() {
        if (visible) {
            String url = String.format("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=%s&steamid=%s&format=json&include_played_free_games=1&include_appinfo=1", apiKey, steamId);
            JSONObject jsonData = new JSONObject(getJson(url));
            gamesDict = jsonData.getJSONObject("response").getJSONArray("games").toList();
            return gamesDict;
        } else {
            throw new RuntimeException("Private profile. Cannot retrieve games.");
        }
    }

    // Other methods like getItems, getSteamId, etc., similarly translated

    public List<Integer> getGifts() {
        String url = String.format("http://steamcommunity.com/profiles/%s/inventory/json/753/1/", steamId);
        JSONObject jsonData = new JSONObject(getJson(url));
        
        List<Integer> giftsList = new ArrayList<>();
        if (jsonData.has("rgDescriptions")) {
            JSONObject rgDescriptions = jsonData.getJSONObject("rgDescriptions");
            Iterator<String> keys = rgDescriptions.keys();
            while (keys.hasNext()) {
                String itemKey = keys.next();
                String link = rgDescriptions.getJSONObject(itemKey).getJSONArray("actions").getJSONObject(0).getString("link");
                Matcher matcher = Pattern.compile("([0-9]+)").matcher(link);
                if (matcher.find()) {
                    giftsList.add(Integer.valueOf(matcher.group(1)));
                }
            }
        }
        return giftsList;
    }

    public List<Integer> getWishlist() {
        String url = String.format("http://steamcommunity.com/profiles/%s/wishlist", steamId);
        Document soup = Jsoup.parse(openUrl(url));
        List<Element> wishGames = soup.select("div.wishlistRow");

        List<Integer> allGames = new ArrayList<>();
        for (Element game : wishGames) {
            String currentId = game.attr("id");
            Matcher matcher = Pattern.compile("([0-9]+)").matcher(currentId);
            if (matcher.find()) {
                allGames.add(Integer.valueOf(matcher.group(1)));
            }
        }
        return allGames;
    }

    // Method getGroups, etc. similarly translated...
}
