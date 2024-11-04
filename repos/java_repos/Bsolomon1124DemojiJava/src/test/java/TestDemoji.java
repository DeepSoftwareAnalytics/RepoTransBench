import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

public class TestDemoji {

    private String tweet;
    
    private final String personTippingHand = "💁";  // length 1
    private final String manTippingHand = "💁‍♂️";  // length 4
    private final String womanTippingHand = "💁‍♀️";  // length 4

    @BeforeEach
    public void setUp() {
        tweet = "#startspreadingthenews yankees win great start by 🎅🏾 going 5strong innings with 5k’s🔥 🐂\n" +
                "solo homerun 🌋🌋 with 2 solo homeruns and👹 3run homerun… 🤡 🚣🏼 👨🏽‍⚖️ with rbi’s … 🔥🔥\n" +
                "🇲🇽 and 🇳🇮 to close the game🔥🔥!!!….\n" +
                "WHAT A GAME!!..\n";
    }

    @Test
    public void testSetup() {
        assertEquals(1, personTippingHand.length());
        assertEquals(4, manTippingHand.length());
        assertEquals(4, womanTippingHand.length());
    }

    @Test
    public void testLastDownloadedTimestampRetType() {
        LocalDateTime ts = Demoji.lastDownloadedTimestamp();
        assertNotNull(ts);
    }

    @Test
    public void testDemojiMain() {
        assertTrue(Demoji.findall("Hi").isEmpty());
        assertEquals("Hi", Demoji.replace("Hi"));
        assertTrue(Demoji.findall("2 ! $&%((@)# $)@ ").isEmpty());
        assertEquals(Map.of("🌓", "first quarter moon"), Demoji.findall("The 🌓 shall rise again"));
        
        String allhands = String.format("Someone actually gets paid to make a %s, a %s, and a %s", 
                                         personTippingHand, manTippingHand, womanTippingHand);
        assertEquals(Map.of(personTippingHand, "person tipping hand", 
                            manTippingHand, "man tipping hand", 
                            womanTippingHand, "woman tipping hand"), 
                     Demoji.findall(allhands));
        assertEquals("Someone actually gets paid to make a , a , and a ", Demoji.replace(allhands));
        assertEquals("Someone actually gets paid to make a X, a X, and a X", Demoji.replace(allhands, "X"));
        assertNotNull(Demoji.lastDownloadedTimestamp());

        String[] batch = {
            "😀", "😂", "🤩", "🤐", "🤢", "🙁", "😫", "🙀", "💓", "🧡", "🖤", "👁️‍🗨️",
            "✋", "🤙", "👊", "🙏", "👂", "👱‍♂️", "🧓", "🙍‍♀️", "🙋", "🙇", "👩‍⚕️",
            "👩‍🔧", "👨‍🚒", "👼", "🦸", "🧝‍♀️", "👯‍♀️", "🤽", "🤼‍♀️", "🏴", "👩‍👧‍👦",
            "🐷", "2️⃣", "8️⃣", "🆖", "🈳", "الجزيرة‎", "傳騰訊入股Reddit 言論自由不保?",
            "🇩🇯", "⬛", "🔵", "🇨🇫", "‼"
        };
        assertEquals(batch.length - 2, Demoji.findall(String.join(" xxx ", batch)).size());
        assertEquals(Map.of("🔥", "fire", "🌋", "volcano", "👨🏽‍⚖️", "man judge: medium skin tone",
                            "🎅🏾", "Santa Claus: medium-dark skin tone", "🇲🇽", "flag: Mexico", "👹", "ogre",
                            "🤡", "clown face", "🇳🇮", "flag: Nicaragua", "🚣🏼", "person rowing boat: medium-light skin tone",
                            "🐂", "ox"), 
                     Demoji.findall(tweet));
    }

    @Test
    public void testFindallList() {
        assertEquals(Demoji.findallList(tweet, true).size(), Demoji.findallList(tweet, false).size());
        assertFalse(Demoji.findallList(tweet, true).isEmpty());
        assertFalse(Demoji.findallList(tweet, false).isEmpty());
        assertTrue(Demoji.findallList(tweet, true).get(0).toLowerCase().contains("santa claus"));
        assertEquals("🔥", Demoji.findallList(tweet, false).get(1));
    }

    @Test
    public void testReplaceWithDesc() {
        assertEquals("#startspreadingthenews yankees win great start by :Santa Claus: medium-dark skin tone: going 5strong innings with 5k’s:fire: :ox:\n" +
                     "solo homerun :volcano::volcano: with 2 solo homeruns and:ogre: 3run homerun… :clown face: :person rowing boat: medium-light skin tone: :man judge: medium skin tone: with rbi’s … :fire::fire:\n" +
                     ":flag: Mexico: and :flag: Nicaragua: to close the game:fire::fire:!!!….\n" +
                     "WHAT A GAME!!..\n", 
                     Demoji.replaceWithDesc(tweet, ":"));
        assertEquals("#startspreadingthenews yankees win great start by |Santa Claus: medium-dark skin tone| going 5strong innings with 5k’s|fire| |ox|\n" +
                     "solo homerun |volcano||volcano| with 2 solo homeruns and|ogre| 3run homerun… |clown face| |person rowing boat: medium-light skin tone| |man judge: medium skin tone| with rbi’s … |fire||fire|\n" +
                     "|flag: Mexico| and |flag: Nicaragua| to close the game|fire||fire|!!!….\n" +
                     "WHAT A GAME!!..\n", 
                     Demoji.replaceWithDesc(tweet, "|"));
    }
}
