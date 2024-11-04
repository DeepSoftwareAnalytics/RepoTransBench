package test.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.json.JSONArray;
import org.json.JSONObject;

import main.java.Utils;
import org.sqlite.SQLiteDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestSaveCheckin {

    private static Connection connection;

    @BeforeAll
    public static void setupDatabase() throws Exception {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite::memory:");
        connection = ds.getConnection();

        String jsonData = new String(Files.readAllBytes(Paths.get("src/test/resources/checkin.json")));
        JSONObject checkinData = new JSONObject(jsonData);

        // Assume saveCheckin, ensureForeignKeys, and createViews are implemented in Utils.java
        Utils.saveCheckin(checkinData, connection);
        Utils.ensureForeignKeys(connection);
        Utils.createViews(connection);
    }

    @Test
    public void testTables() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
        Set<String> tables = Set.of(
            "venues", "categories", "with", "users", "likes", "categories_venues", "sources",
            "checkins", "photos", "categories_events", "events", "posts", "post_sources", "stickers"
        );

        while (rs.next()) {
            assertTrue(tables.contains(rs.getString("name")));
        }
        rs.close();
        stmt.close();
    }

    @Test
    public void testVenue() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM venues WHERE id='453774dcf964a520bd3b1fe3'");
        assertTrue(rs.next());
        assertEquals("Restaurant Name", rs.getString("name"));
        assertEquals("Address", rs.getString("address"));
        assertEquals("at cross street", rs.getString("crossStreet"));
        assertEquals("94xxx", rs.getString("postalCode"));
        assertEquals("US", rs.getString("cc"));
        assertEquals("City", rs.getString("city"));
        assertEquals("State", rs.getString("state"));
        assertEquals("Country", rs.getString("country"));
        assertEquals("[\"Address (at cross street)\", \"City, State, Zip\", \"Country\"]", rs.getString("formattedAddress"));
        assertEquals(38.456, rs.getDouble("latitude"));
        assertEquals(-122.345, rs.getDouble("longitude"));

        // Test categories
        rs = stmt.executeQuery(
            "SELECT * FROM categories WHERE id IN (SELECT categories_id FROM categories_venues WHERE venues_id='453774dcf964a520bd3b1fe3')"
        );
        assertTrue(rs.next());
        assertEquals("4bf58dd8d48988d10c941735", rs.getString("id"));
        assertEquals("Category Name", rs.getString("name"));
        assertEquals("Category Names", rs.getString("pluralName"));
        assertEquals("Category", rs.getString("shortName"));
        assertEquals("https://ss3.4sqi.net/img/categories_v2/food/french_", rs.getString("icon_prefix"));
        assertEquals(".png", rs.getString("icon_suffix"));
        assertEquals(1, rs.getInt("primary"));
        rs.close();
        stmt.close();
    }

    @Test
    public void testEvent() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM events WHERE id='5bf8e4fb646e38002c472397'");
        assertTrue(rs.next());
        assertEquals("A movie", rs.getString("name"));
        rs.close();

        // Test categories for the event
        rs = stmt.executeQuery(
            "SELECT * FROM categories WHERE id IN (SELECT categories_id FROM categories_events WHERE events_id='5bf8e4fb646e38002c472397')"
        );
        assertTrue(rs.next());
        assertEquals("4dfb90c6bd413dd705e8f897", rs.getString("id"));
        assertEquals("Movie", rs.getString("name"));
        assertEquals("Movies", rs.getString("pluralName"));
        assertEquals("Movie", rs.getString("shortName"));
        assertEquals("https://ss3.4sqi.net/img/categories_v2/arts_entertainment/movietheater_", rs.getString("icon_prefix"));
        assertEquals(".png", rs.getString("icon_suffix"));
        assertEquals(1, rs.getInt("primary"));
        rs.close();
        stmt.close();
    }

    @Test
    public void testSticker() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM stickers WHERE id='56312102498e50c6f99f1d9b'");
        assertTrue(rs.next());
        assertEquals("Foodie", rs.getString("name"));
        assertEquals("unlockable", rs.getString("stickerType"));
        assertEquals("{\"name\": \"collectible\", \"index\": 47}", rs.getString("group"));
        assertEquals("{\"page\": 1, \"index\": 23}", rs.getString("pickerPosition"));
        assertEquals("teaseText", rs.getString("teaseText"));
        assertEquals("unlockText 😉", rs.getString("unlockText"));
        assertEquals("https://igx.4sqi.net/img/sticker/", rs.getString("image_prefix"));
        assertEquals("[60, 94, 150, 300]", rs.getString("image_sizes"));
        assertEquals("/foodie_a56e26.png", rs.getString("image_name"));
        rs.close();
        stmt.close();
    }

    @Test
    public void testLikes() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT users_id, checkins_id FROM likes");

        List<String[]> likes = new ArrayList<>();
        while (rs.next()) {
            likes.add(new String[]{rs.getString("users_id"), rs.getString("checkins_id")});
        }

        List<String[]> expectedLikes = List.of(
            new String[]{"314", "592b2cfe09e28339ac543fde"},
            new String[]{"323", "592b2cfe09e28339ac543fde"},
            new String[]{"778", "592b2cfe09e28339ac543fde"}
        );

        assertEquals(expectedLikes, likes);

        rs.close();
        stmt.close();
    }

    @Test
    public void testWith() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT users_id, checkins_id FROM with");

        List<String[]> withList = new ArrayList<>();
        while (rs.next()) {
            withList.add(new String[]{rs.getString("users_id"), rs.getString("checkins_id")});
        }

        List<String[]> expectedWith = List.of(
            new String[]{"900", "592b2cfe09e28339ac543fde"}
        );

        assertEquals(expectedWith, withList);

        rs.close();
        stmt.close();
    }

    @Test
    public void testUsers() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM users");

        List<String[]> users = new ArrayList<>();
        while (rs.next()) {
            users.add(new String[]{
                rs.getString("id"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getString("gender"),
                rs.getString("relationship"),
                rs.getString("photo_prefix"),
                rs.getString("photo_suffix")
            });
        }

        List<String[]> expectedUsers = List.of(
            new String[]{"900", "Natalie", "Downe", "female", "friend", "https://fastly.4sqi.net/img/user/", "/nd.jpg"},
            new String[]{"314", "J", "T", "female", "friend", "https://fastly.4sqi.net/img/user/", "/jt.jpg"},
            new String[]{"323", "A", "R", "male", "friend", "https://fastly.4sqi.net/img/user/", "/ar.png"},
            new String[]{"778", "J", null, "none", "friend", "https://fastly.4sqi.net/img/user/", "/j"},
            new String[]{"15889193", "Simon", "Willison", "male", "self", "https://fastly.4sqi.net/img/user/", "/CNGFSAMX00DB4DYZ.jpg"}
        );

        assertEquals(expectedUsers, users);

        rs.close();
        stmt.close();
    }

    @Test
    public void testPhotos() throws Exception {
        Statement stmt = connection.createStatement();

        // Test foreign keys
        ResultSet rs = stmt.executeQuery(
            "PRAGMA foreign_key_list(photos)"
        );
        List<String[]> foreignKeys = new ArrayList<>();
        while (rs.next()) {
            foreignKeys.add(new String[]{rs.getString("table"), rs.getString("from"), rs.getString("to")});
        }

        List<String[]> expectedForeignKeys = List.of(
            new String[]{"users", "user", "id"},
            new String[]{"sources", "source", "id"}
        );

        assertEquals(expectedForeignKeys, foreignKeys);

        // Test photo rows
        rs = stmt.executeQuery("SELECT * FROM photos");

        List<String[]> photos = new ArrayList<>();
        while (rs.next()) {
            photos.add(new String[]{
                rs.getString("id"),
                rs.getString("createdAt"),
                rs.getString("source"),
                rs.getString("prefix"),
                rs.getString("suffix"),
                rs.getString("width"),
                rs.getString("height"),
                rs.getString("visibility"),
                rs.getString("created"),
                rs.getString("user")
            });
        }

        List<String[]> expectedPhotos = List.of(
            new String[]{
                "5b3840f34a7aae002c7845ee", "1530413299", "1", "https://fastly.4sqi.net/img/general/",
                "/15889193_ptDsf3Go3egIPU6WhwC4lIsEQLpW5SXxY3J1YyTY7Wc.jpg", "1920", "1440", "public",
                "2018-07-01T02:48:19", "15889193"
            },
            new String[]{
                "5b38417b16fa04002c718f84", "1530413435", "1", "https://fastly.4sqi.net/img/general/",
                "/15889193_GrExrA5SoKhYBK6VhZ0g97Zy8qcEdqLpuUCJSTxzaWI.jpg", "1920", "1440", "public",
                "2018-07-01T02:50:35", "15889193"
            },
            new String[]{
                "5b38417d04d1ae002c53b844", "1530413437", "1", "https://fastly.4sqi.net/img/general/",
                "/15889193__9cPZDE4Y1dhNgrqueMSFYnv20k4u1hHiqPxw5m3JOc.jpg", "1920", "1440", "public",
                "2018-07-01T02:50:37", "15889193"
            }
        );

        assertEquals(expectedPhotos, photos);

        rs.close();
        stmt.close();
    }

    @Test
    public void testPosts() throws Exception {
        Statement stmt = connection.createStatement();

        // Test foreign keys
        ResultSet rs = stmt.executeQuery(
            "PRAGMA foreign_key_list(posts)"
        );
        List<String[]> foreignKeys = new ArrayList<>();
        while (rs.next()) {
            foreignKeys.add(new String[]{rs.getString("table"), rs.getString("from"), rs.getString("to")});
        }

        List<String[]> expectedForeignKeys = List.of(
            new String[]{"checkins", "checkin", "id"},
            new String[]{"post_sources", "post_source", "id"}
        );

        assertEquals(expectedForeignKeys, foreignKeys);

        // Test post rows
        rs = stmt.executeQuery("SELECT * FROM posts");

        List<String[]> posts = new ArrayList<>();
        while (rs.next()) {
            posts.add(new String[]{
                rs.getString("id"),
                rs.getString("createdAt"),
                rs.getString("text"),
                rs.getString("url"),
                rs.getString("contentId"),
                rs.getString("created"),
                rs.getString("post_source"),
                rs.getString("checkin")
            });
        }

        List<String[]> expectedPosts = List.of(
            new String[]{
                "58994045e386e304939156e0", "1486438469",
                "The samosa chaat appetizer (easily enough for two or even four people) was a revelation - I've never tasted anything quite like it before, absolutely delicious. Chicken tika masala was amazing too.",
                "https://foursquare.com/item/58994045668af77dae50b376", "58994045668af77dae50b376",
                "2017-02-07T03:34:29", "UJXJTUHR42CKGO54KXQWGUZJL3OJKMKMVHGJ1SWIOC5TRKAC", "592b2cfe09e28339ac543fde"
            }
        );

        assertEquals(expectedPosts, posts);

        rs.close();
        stmt.close();
    }

    @Test
    public void testCheckinWithNoEvent() throws Exception {
        // Load checkin data from the JSON file
        String jsonData = new String(Files.readAllBytes(Paths.get("src/test/resources/checkin.json")));
        JSONObject checkinData = new JSONObject(jsonData);
        checkinData.remove("event");  // Remove the "event" key

        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite::memory:");
        Connection tempConnection = ds.getConnection();

        // Save the checkin with no event
        Utils.saveCheckin(checkinData, tempConnection);

        Statement stmt = tempConnection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM checkins");

        assertTrue(rs.next());
        assertEquals(null, rs.getString("event"));

        rs.close();
        stmt.close();
        tempConnection.close();
    }

    @Test
    public void testView() throws Exception {
        Statement stmt = connection.createStatement();
    
        // 检查视图名称
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='view'");
        Set<String> expectedViews = Set.of("checkin_details", "venue_details");
        Set<String> actualViews = new HashSet<>();
        while (rs.next()) {
            actualViews.add(rs.getString("name"));
        }
        assertEquals(expectedViews, actualViews);
    
        // 测试 checkin_details 视图中的数据
        rs = stmt.executeQuery("SELECT * FROM checkin_details WHERE id = '592b2cfe09e28339ac543fde'");
        assertTrue(rs.next());
        assertEquals("592b2cfe09e28339ac543fde", rs.getString("id"));
        assertEquals("2017-05-28T20:03:10", rs.getString("created"));
        assertEquals("453774dcf964a520bd3b1fe3", rs.getString("venue_id"));
        assertEquals("Restaurant Name", rs.getString("venue_name"));
        assertEquals(38.456, rs.getDouble("latitude"), 0.001);
        assertEquals(-122.345, rs.getDouble("longitude"), 0.001);
        assertEquals("Category Name", rs.getString("venue_categories"));
        assertEquals("7th wedding anniversary — with Natalie", rs.getString("shout"));
        assertNull(rs.getString("createdBy"));
        assertEquals("A movie", rs.getString("event_name"));
    
        rs.close();
    
        // 测试 venue_details 视图中的数据
        rs = stmt.executeQuery("SELECT * FROM venue_details WHERE id = '453774dcf964a520bd3b1fe3'");
        assertTrue(rs.next());
        assertEquals("453774dcf964a520bd3b1fe3", rs.getString("id"));
        assertEquals("2017-05-28T20:03:10", rs.getString("first"));
        assertEquals("2017-05-28T20:03:10", rs.getString("last"));
        assertEquals(1, rs.getInt("count"));
        assertEquals("Category Name", rs.getString("venue_categories"));
        assertEquals("Restaurant Name", rs.getString("name"));
        assertEquals("Address", rs.getString("address"));
        assertEquals("at cross street", rs.getString("crossStreet"));
        assertEquals("94xxx", rs.getString("postalCode"));
        assertEquals("US", rs.getString("cc"));
        assertEquals("City", rs.getString("city"));
        assertEquals("State", rs.getString("state"));
        assertEquals("Country", rs.getString("country"));
        assertEquals("[\"Address (at cross street)\", \"City, State, Zip\", \"Country\"]", rs.getString("formattedAddress"));
        assertEquals(38.456, rs.getDouble("latitude"), 0.001);
        assertEquals(-122.345, rs.getDouble("longitude"), 0.001);
    
        rs.close();
        stmt.close();
    }    
}
