import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class CalltreeConverterTest {

    private MockTimeProfile profile;

    @BeforeEach
    public void setUp() throws Exception {
        this.profile = new MockTimeProfile();
        this.profile.start("top");
        ProfileCode.top();
        this.profile.stop();
    }

    @Test
    public void testDirectEntries() throws Exception {
        List<Entry> entries = profile.getEntries();
        CalltreeConverter converter = new CalltreeConverter(entries, new Scale("ns"));
        StringWriter outFile = new StringWriter();
        
        converter.output(outFile);
        Assertions.assertEquals(ProfileCode.expected_output, outFile.toString());
    }

    @Test
    public void testPstatsData() throws Exception {
        // Simulate the behavior of pstats with entry data
        List<Entry> entries = profile.getEntries();
        CalltreeConverter converter = new CalltreeConverter(entries, new Scale("ns"));
        StringWriter outFile = new StringWriter();
        
        converter.output(outFile);
        Assertions.assertEquals(ProfileCode.expected_output, outFile.toString());
    }
}
