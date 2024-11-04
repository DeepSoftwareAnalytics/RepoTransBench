import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PinboardTest {
    private Pinboard pinboard;
    private String url;

    @BeforeEach
    public void setUp() throws IOException, InterruptedException, ParseException {
        String apiToken = System.getenv("PINBOARD_API_TOKEN");
        pinboard = new Pinboard(apiToken);
        List<Pinboard.Bookmark> bookmarks = pinboard.getRecentPosts(1, new Date());
        url = bookmarks.get(0).getUrl();
    }

    private Pinboard.Bookmark getBookmark() throws IOException, InterruptedException, ParseException {
        List<Pinboard.Bookmark> bookmarks = pinboard.getPosts(url, true);
        return bookmarks.get(0);
    }

    @Test
    public void testAddTagThroughWebsite() throws IOException, InterruptedException, ParseException {
        Pinboard.Bookmark bookmark = getBookmark();
        System.out.println("Click enter after adding a tag to this bookmark through the website (" + bookmark.getUrl().substring(0, 20) + "...)");
        System.in.read();

        Pinboard.Bookmark updatedBookmark = getBookmark();
        assertNotEquals(bookmark.getMeta(), updatedBookmark.getMeta());
    }

    @Test
    public void testRemoveTagThroughWebsite() throws IOException, InterruptedException, ParseException {
        Pinboard.Bookmark bookmark = getBookmark();
        System.out.println("Click enter after removing a tag from this bookmark through the website (" + url.substring(0, 20) + "...)");
        System.in.read();

        Pinboard.Bookmark updatedBookmark = getBookmark();
        assertNotEquals(bookmark.getMeta(), updatedBookmark.getMeta());
    }
}
