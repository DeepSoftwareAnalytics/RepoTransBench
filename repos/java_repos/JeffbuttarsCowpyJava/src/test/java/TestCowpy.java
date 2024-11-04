import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set; // <-- Add this import

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TestCowpy {
    
    @Test
    void testMooseMessage() {
        Cowacter cheese = new Moose();
        String msg = cheese.milk("My witty message");
        System.out.println(msg);
        assertTrue(msg.contains("My witty message"), "The message should contain the user's message");
    }

    @Test
    void testMooseThoughtMessage() {
        Cowacter cheese = new Moose();
        cheese.withThoughts(true);
        String msg = cheese.milk("My witty message, with thought");
        System.out.println(msg);
        assertTrue(msg.contains("My witty message, with thought"), "The message should contain the user's message with thought");
    }

    @Test
    void testMooseTongueMessage() {
        Cowacter cheese = new Moose();
        cheese.withTongue(true);
        String msg = cheese.milk("My witty message, with tongue");
        System.out.println(msg);
        assertTrue(msg.contains("My witty message, with tongue"), "The message should contain the user's message with tongue");
    }

    @Test
    void testMooseDeadEyesMessage() {
        Cowacter cheese = new Moose();
        cheese.withEyes("dead");
        String msg = cheese.milk("my witty message, i'm dead");
        System.out.println(msg);
        assertTrue(msg.contains("my witty message, i'm dead"), "The message should contain the user's message with dead eyes");
    }

    @Test
    void testGetCowByName() {
        Cowacter cheese = Cow.getCow("moose");
        String msg = cheese.milk("Cow by name is moose");
        System.out.println(msg);
        assertTrue(msg.contains("Cow by name is moose"), "The message should contain the user's message for moose");
    }

    @Test
    void testMooseMultipleAttributes() {
        Cowacter cheese = new Moose();
        cheese.withThoughts(true).withTongue(true).withEyes("dead");
        String msg = cheese.milk("My witty message with several attributes");
        System.out.println(msg);
        assertTrue(msg.contains("My witty message with several attributes"), "The message should contain the user's message with several attributes");
    }

    @Test
    void testRandomCowMessage() {
        String msg = Cow.milkRandomCow("A random message for fun", true);
        System.out.println(msg);
        assertTrue(msg.contains("A random message for fun"), "The message should contain the user's message randomly");
    }

    @Test
    void testRandomCowMultilineMessage() {
        String msg = Cow.milkRandomCow(String.join("\n", 
            "A random multi-line message:", 
            "1. for fun", 
            "2. for fun", 
            "3. and for fun"
        ), true);
        System.out.println(msg);
        assertTrue(msg.contains("A random multi-line message:"), "The message should contain multiple lines");
    }

    @Test
    void testEyeOptions() {
        Set<String> eyeOptions = Cow.eyeOptions();
        System.out.println(eyeOptions);
        assertTrue(eyeOptions.contains("default"), "Eye options should contain 'default'");
    }

    @Test
    void testCowOptions() {
        Set<String> cowOptions = Cow.cowOptions();
        System.out.println(cowOptions);
        assertTrue(cowOptions.contains("default"), "Cow options should contain 'default'");
    }
}

