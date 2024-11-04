import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MatcherTestCase {

    @Test
    public void testMatcherWith2DigitsDates() {
        String header = String.join("\n",
                "//",
                "//  MyFile.swift",
                "//  MyCompany",
                "//",
                "//  Created by John Appleseed on 12/18/15.",
                "//  Copyright (c) 2015 MyCompany. All rights reserved.",
                "//");

        HeaderMatcher matcher = new HeaderMatcher(header, false);
        assertNotNull(matcher.match());
    }

    @Test
    public void testMatcherWith1DigitMonth() {
        String header = String.join("\n",
                "//",
                "//  MyFile.swift",
                "//  MyCompany",
                "//",
                "//  Created by John Appleseed on 2/18/15.",
                "//  Copyright (c) 2015 MyCompany. All rights reserved.",
                "//");

        HeaderMatcher matcher = new HeaderMatcher(header, false);
        assertNotNull(matcher.match());
    }

    @Test
    public void testMatcherWith1DigitDay() {
        String header = String.join("\n",
                "//",
                "//  MyFile.swift",
                "//  MyCompany",
                "//",
                "//  Created by John Appleseed on 12/1/15.",
                "//  Copyright (c) 2015 MyCompany. All rights reserved.",
                "//");

        HeaderMatcher matcher = new HeaderMatcher(header, false);
        assertNotNull(matcher.match());
    }

    @Test
    public void testMatcherWithObjcHeaderFile() {
        String header = String.join("\n",
                "//",
                "//  MyFile.h",
                "//  MyCompany",
                "//",
                "//  Created by John Appleseed on 12/18/15.",
                "//  Copyright (c) 2015 MyCompany. All rights reserved.",
                "//");

        HeaderMatcher matcher = new HeaderMatcher(header, false);
        assertNotNull(matcher.match());
    }

    @Test
    public void testMatcherWithObjcImplementationFile() {
        String header = String.join("\n",
                "//",
                "//  MyFile.m",
                "//  MyCompany",
                "//",
                "//  Created by John Appleseed on 12/18/15.",
                "//  Copyright (c) 2015 MyCompany. All rights reserved.",
                "//");

        HeaderMatcher matcher = new HeaderMatcher(header, false);
        assertNotNull(matcher.match());
    }

    @Test
    public void testMatcherWithSpecialCopyrightCharacter() {
        String header = String.join("\n",
                "//",
                "//  MyFile.m",
                "//  MyCompany",
                "//",
                "//  Created by John Appleseed on 12/18/15.",
                "//  Copyright © 2015 MyCompany. All rights reserved.",
                "//");

        HeaderMatcher matcher = new HeaderMatcher(header, false);
        assertNotNull(matcher.match());
    }

    @Test
    public void testMatcherWithTrimNewLinesOn() {
        String header = String.join("\n",
                "",
                "",
                "//",
                "//  MyFile.m",
                "//  MyCompany",
                "//",
                "//  Created by John Appleseed on 12/18/15.",
                "//  Copyright © 2015 MyCompany. All rights reserved.",
                "//",
                "",
                "");

        HeaderMatcher matcher = new HeaderMatcher(header, true);
        assertNotNull(matcher.match());
    }

    @Test
    public void testMatcherWithTrimNewLinesOff() {
        String header = String.join("\n",
                "",
                "",
                "//",
                "//  MyFile.m",
                "//  MyCompany",
                "//",
                "//  Created by John Appleseed on 12/18/15.",
                "//  Copyright © 2015 MyCompany. All rights reserved.",
                "//",
                "",
                "");

        HeaderMatcher matcher = new HeaderMatcher(header, false);
        assertNull(matcher.match());
    }
}
