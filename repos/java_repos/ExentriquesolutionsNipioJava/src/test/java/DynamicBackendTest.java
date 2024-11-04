import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class DynamicBackendTest {
    private final InputStream originalSysIn = System.in;
    private final PrintStream originalSysOut = System.out;
    private final PrintStream originalSysErr = System.err;

    private MockedStatic<System> systemMock;
    private DynamicBackend backend;

    @BeforeEach
    void setUp() {
        systemMock = mockStatic(System.class);
        backend = spy(new DynamicBackend());

        ByteArrayInputStream in = new ByteArrayInputStream("5\t100\n".getBytes());
        System.setIn(in);
        System.setErr(originalSysErr);
        systemMock.when(() -> System.getenv()).thenReturn(System.getenv());
    }

    @AfterEach
    void tearDown() {
        systemMock.close();
        System.setOut(originalSysOut);
        System.setErr(originalSysErr);
        System.setIn(originalSysIn);
    }

    @Test
    void testBackendEndsResponseToAnyRequestIfIpIsBlacklisted() {
        when(System.getenv("NIPIO_BLACKLIST")).thenReturn("black_listed=127.0.0.2");
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.0.2.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tBlacklisted: 127.0.0.2\nEND\n");
    }

    @Test
    void testBackendEndsResponseToARequestIfIpIsBlacklisted() {
        when(System.getenv("NIPIO_BLACKLIST")).thenReturn("black_listed=127.0.0.2");
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.0.2.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tBlacklisted: 127.0.0.2\nEND\n");
    }

    @Test
    void testBackendEndsResponseToAnyRequestIfIpIsNotWhitelisted() {
        when(System.getenv("NIPIO_WHITELIST")).thenReturn("whitelist1=10.0.0.0/8");
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.10.0.10.1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tNot Whitelisted: 10.0.10.1\nEND\n");
    }

    @Test
    void testBackendEndsResponseToARequestIfIpIsNotWhitelisted() {
        when(System.getenv("NIPIO_WHITELIST")).thenReturn("whitelist1=10.0.0.0/8");
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.10.0.10.1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tNot Whitelisted: 10.0.10.1\nEND\n");
    }

    @Test
    void testBackendWithEmptyWhitelistRespondsToAnyRequestForValidIp() {
        when(System.getenv("NIPIO_WHITELIST")).thenReturn("");
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.10.0.10.1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain.10.0.10.1.nip.io.test\tIN\tA\t200\t22\t10.0.10.1\n",
            "DATA\t0\t1\tsubdomain.10.0.10.1.nip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tsubdomain.10.0.10.1.nip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendWithEmptyWhitelistRespondsToARequestForValidIp() {
        when(System.getenv("NIPIO_WHITELIST")).thenReturn("");
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.10.0.10.1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain.10.0.10.1.nip.io.test\tIN\tA\t200\t22\t10.0.10.1\n",
            "DATA\t0\t1\tsubdomain.10.0.10.1.nip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tsubdomain.10.0.10.1.nip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendRespondsToAnyRequestWithValidIp() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.0.1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain.127.0.0.1.nip.io.test\tIN\tA\t200\t22\t127.0.0.1\n",
            "DATA\t0\t1\tsubdomain.127.0.0.1.nip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tsubdomain.127.0.0.1.nip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendRespondsToARequestWithValidIp() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.0.1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain.127.0.0.1.nip.io.test\tIN\tA\t200\t22\t127.0.0.1\n",
            "DATA\t0\t1\tsubdomain.127.0.0.1.nip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tsubdomain.127.0.0.1.nip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendRespondsToAnyRequestWithValidIpSeparatedByDashes() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain-127-0-0-1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain-127-0-0-1.nip.io.test\tIN\tA\t200\t22\t127.0.0.1\n",
            "DATA\t0\t1\tsubdomain-127-0-0-1.nip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tsubdomain-127-0-0-1.nip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendRespondsToARequestWithValidIpSeparatedByDashes() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain-127-0-0-1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain-127-0-0-1.nip.io.test\tIN\tA\t200\t22\t127.0.0.1\n",
            "DATA\t0\t1\tsubdomain-127-0-0-1.nip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tsubdomain-127-0-0-1.nip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendRespondsToARequestWithValidIpHexstring() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tuser-deadbeef.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tuser-deadbeef.nip.io.test\tIN\tA\t200\t22\t222.173.190.239\n",
            "DATA\t0\t1\tuser-deadbeef.nip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tuser-deadbeef.nip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendRespondsToLongHexstringWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tdeadbeefcafe.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: deadbeefcafe.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToShortHexstringWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tuser-dec0ded.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: user-dec0ded.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToInvalidHexstringWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tdeadcode.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: deadcode.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToInvalidIpInAnyRequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: subdomain.127.0.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToInvalidIpInARequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: subdomain.127.0.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToShortIpInAnyRequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\t127.0.1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: 127.0.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToShortIpInARequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\t127.0.1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: 127.0.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToLargeIpInAnyRequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.300.1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: subdomain.127.0.300.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToLargeIpInARequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.300.1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: subdomain.127.0.300.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToStringInIpInAnyRequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.STRING.1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: subdomain.127.0.string.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToStringInIpInARequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.STRING.1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: subdomain.127.0.string.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToNoIpInAnyRequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.1.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: subdomain.127.0.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToNoIpInARequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tInvalid IP address: subdomain.127.0.1.nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToSelfDomainToARequest() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tnip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tnip.io.test\tIN\tA\t200\t22\t127.0.0.33\n",
            "DATA\t0\t1\tnip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tnip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendRespondsToSelfDomainToAnyRequest() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tnip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tnip.io.test\tIN\tA\t200\t22\t127.0.0.33\n",
            "DATA\t0\t1\tnip.io.test\tIN\tNS\t200\t22\tns1.nip.io.test\n",
            "DATA\t0\t1\tnip.io.test\tIN\tNS\t200\t22\tns2.nip.io.test\n"
        );
    }

    @Test
    void testBackendRespondsToNameServersARequestWithValidIp() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tns1.nip.io.test\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("DATA\t0\t1\tns1.nip.io.test\tIN\tA\t200\t22\t127.0.0.34\n");
    }

    @Test
    void testBackendRespondsToNameServersAnyRequestWithValidIp() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tns2.nip.io.test\tIN\tANY\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("DATA\t0\t1\tns2.nip.io.test\tIN\tA\t200\t22\t127.0.0.35\n");
    }

    @Test
    void testBackendRespondsToSOARequestForSelf() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tnip.io.test\tIN\tSOA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("DATA\t0\t1\tnip.io.test\tIN\tSOA\t200\t22\tMY_SOA\n");
    }

    @Test
    void testBackendRespondsToSOARequestForValidIp() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.0.1.nip.io.test\tIN\tSOA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain.127.0.0.1.nip.io.test\tIN\tSOA\t200\t22\tMY_SOA\n"
        );
    }

    @Test
    void testBackendRespondsToSOARequestForInvalidIp() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.127.0.1.nip.io.test\tIN\tSOA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain.127.0.1.nip.io.test\tIN\tSOA\t200\t22\tMY_SOA\n"
        );
    }

    @Test
    void testBackendRespondsToSOARequestForNoIp() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tsubdomain.nip.io.test\tIN\tSOA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains(
            "DATA\t0\t1\tsubdomain.nip.io.test\tIN\tSOA\t200\t22\tMY_SOA\n"
        );
    }

    @Test
    void testBackendRespondsToSOARequestForNameServer() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tns1.nip.io.test\tIN\tSOA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("DATA\t0\t1\tns1.nip.io.test\tIN\tSOA\t200\t22\tMY_SOA\n");
    }

    @Test
    void testBackendRespondsToARequestForUnknownDomainWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tunknown.domain\tIN\tA\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tUnknown type: A, domain: unknown.domain\nEND\n");
    }

    @Test
    void testBackendRespondsToInvalidRequestWithInvalidResponse() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "Q\tnip.io.test\tIN\tINVALID\t1\t127.0.0.1\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        assertSysOutContains("LOG\tUnknown type: INVALID, domain: nip.io.test\nEND\n");
    }

    @Test
    void testBackendRespondsToInvalidCommandWithFail() {
        ByteArrayInputStream in = new ByteArrayInputStream(("HELO\t5\n" +
                "INVALID\tCOMMAND\n" +
                "END\n").getBytes());
        System.setIn(in);

        backend.run();

        PrintStream out = mock(PrintStream.class);
        System.setOut(out);

        verify(out).println("OK");
        verify(out).println("nip.io backend - We are good");
        verify(out).println("FAIL");
        verify(out).println("END");
    }

    @Test
    void testConfigureWithFullConfig() {
        when(System.getenv("NIPIO_DOMAIN")).thenReturn("nip.io.test");
        when(System.getenv("NIPIO_TTL")).thenReturn("1000");
        when(System.getenv("NIPIO_NONWILD_DEFAULT_IP")).thenReturn("127.0.0.40");
        when(System.getenv("NIPIO_SOA_ID")).thenReturn("55");
        when(System.getenv("NIPIO_SOA_HOSTMASTER")).thenReturn("emailaddress@nip.io.test");
        when(System.getenv("NIPIO_SOA_NS")).thenReturn("ns1.nip.io.test");
        when(System.getenv("NIPIO_SOA_REFRESH")).thenReturn("56");
        when(System.getenv("NIPIO_SOA_RETRY")).thenReturn("57");
        when(System.getenv("NIPIO_SOA_EXPIRY")).thenReturn("58");
        when(System.getenv("NIPIO_SOA_MINIMUM_TTL")).thenReturn("59");
        when(System.getenv("NIPIO_NAMESERVERS")).thenReturn("ns1.nip.io.test=127.0.0.41 ns2.nip.io.test=127.0.0.42");
        when(System.getenv("NIPIO_WHITELIST")).thenReturn("whitelist1=127.0.0.0/8 whitelist2=192.168.0.0/16");
        when(System.getenv("NIPIO_BLACKLIST")).thenReturn("black_listed=10.0.0.100");

        DynamicBackend backend = new DynamicBackend();
        backend.configure();

        assertThat(backend.getId()).isEqualTo("55");
        assertThat(backend.getIpAddress()).isEqualTo("127.0.0.40");
        assertThat(backend.getDomain()).isEqualTo("nip.io.test");
        assertThat(backend.getTtl()).isEqualTo("1000");
        assertThat(backend.getNameServers()).isEqualTo(Map.of(
            "ns1.nip.io.test", "127.0.0.41",
            "ns2.nip.io.test", "127.0.0.42"
        ));
        assertThat(backend.getWhitelistedRanges()).containsExactlyInAnyOrder(
            "127.0.0.0/8",
            "192.168.0.0/16"
        );
        assertThat(backend.getBlacklistedIps()).containsExactly("10.0.0.100");
        assertThat(backend.getSoa()).isEqualTo("ns1.nip.io.test emailaddress@nip.io.test 55 56 57 58 59");
    }

    @Test
    void testConfigureWithEnvironmentVariablesSet() {
        when(System.getenv("NIPIO_DOMAIN")).thenReturn("example.com");
        when(System.getenv("NIPIO_TTL")).thenReturn("1000");
        when(System.getenv("NIPIO_NONWILD_DEFAULT_IP")).thenReturn("127.0.0.30");
        when(System.getenv("NIPIO_SOA_ID")).thenReturn("99");
        when(System.getenv("NIPIO_SOA_HOSTMASTER")).thenReturn("hostmaster@example.com");
        when(System.getenv("NIPIO_SOA_NS")).thenReturn("ns1.example.com");
        when(System.getenv("NIPIO_SOA_REFRESH")).thenReturn("40");
        when(System.getenv("NIPIO_SOA_RETRY")).thenReturn("41");
        when(System.getenv("NIPIO_SOA_EXPIRY")).thenReturn("42");
        when(System.getenv("NIPIO_SOA_MINIMUM_TTL")).thenReturn("43");
        when(System.getenv("NIPIO_NAMESERVERS")).thenReturn("ns1.example.com=127.0.0.31 ns2.example.com=127.0.0.32");
        when(System.getenv("NIPIO_WHITELIST")).thenReturn("whitelist1=10.0.0.0/8");
        when(System.getenv("NIPIO_BLACKLIST")).thenReturn("black_listed=10.0.0.111 black_listed2=10.0.0.112");

        DynamicBackend backend = new DynamicBackend();
        backend.configure();

        assertThat(backend.getId()).isEqualTo("99");
        assertThat(backend.getIpAddress()).isEqualTo("127.0.0.30");
        assertThat(backend.getDomain()).isEqualTo("example.com");
        assertThat(backend.getTtl()).isEqualTo("1000");
        assertThat(backend.getNameServers()).isEqualTo(Map.of(
            "ns1.example.com", "127.0.0.31",
            "ns2.example.com", "127.0.0.32"
        ));
        assertThat(backend.getWhitelistedRanges()).containsExactly("10.0.0.0/8");
        assertThat(backend.getBlacklistedIps()).containsExactly("10.0.0.111", "10.0.0.112");
        assertThat(backend.getSoa()).isEqualTo("ns1.example.com hostmaster@example.com 99 40 41 42 43");
    }

    @Test
    void testConfigureWithEnvListsConfig() {
        when(System.getenv("NIPIO_WHITELIST")).thenReturn("whitelist1=10.0.0.0/8");
        when(System.getenv("NIPIO_BLACKLIST")).thenReturn("black_listed=10.0.0.111 black_listed2=10.0.0.112");

        DynamicBackend backend = new DynamicBackend();
        backend.configure("src/test/resources/backend_test_no_lists.conf");

        assertThat(backend.getWhitelistedRanges()).containsExactly("10.0.0.0/8");
        assertThat(backend.getBlacklistedIps()).containsExactly("10.0.0.111", "10.0.0.112");
    }

    @Test
    void testConfigureWithConfigMissingLists() {
        when(System.getenv("NIPIO_WHITELIST")).thenReturn("");
        when(System.getenv("NIPIO_BLACKLIST")).thenReturn("");

        DynamicBackend backend = new DynamicBackend();
        backend.configure();

        assertThat(backend.getWhitelistedRanges()).isEmpty();
        assertThat(backend.getBlacklistedIps()).isEmpty();
    }

    private void assertSysOutContains(String... expectedLines) {
        String output = getOutputString();
        for (String line : expectedLines) {
            assertThat(output).contains(line);
        }
    }

    private String getOutputString() {
        PrintStream out = mock(PrintStream.class);
        System.setOut(out);
        return out.toString();
    }

    void runBackend() {
        backend = createBackend();
        backend.run();
    }

    void runBackendWithoutWhitelist() {
        backend = createBackend();
        backend.setWhitelistedRanges(Collections.emptyList());
        backend.run();
    }

    void sendCommands(String... commands) {
        List<String> commandsToSend = new ArrayList<>();
        commandsToSend.add("HELO\t5\n");

        for (String command : commands) {
            commandsToSend.add(command.replace(" ", "\t") + "\n");
        }

        commandsToSend.add("END\n");

        // Mocking System.in to simulate input
        ByteArrayInputStream in = new ByteArrayInputStream(String.join("", commandsToSend).getBytes());
        System.setIn(in);
    }

    void assertExpectedResponses(String... responses) {
        PrintStream out = mock(PrintStream.class);
        System.setOut(out);

        List<String> calls = new ArrayList<>(Arrays.asList(
            "OK", "\t", "nip.io backend - We are good", "\n"
        ));

        for (String response : responses) {
            String[] splitResponse = response.split("\t");
            for (String item : splitResponse) {
                calls.add(item);
                calls.add("\t");
            }
            calls.remove(calls.size() - 1); // Remove last tab
            calls.add("\n");
        }

        calls.addAll(Arrays.asList("END", "\n"));

        for (String call : calls) {
            verify(out).print(call);
        }

        verify(out, times(calls.size())).print(anyString());
    }

    static DynamicBackend createBackend() {
        DynamicBackend backend = new DynamicBackend();
        backend.setId("22");
        backend.setSoa("MY_SOA");
        backend.setIpAddress("127.0.0.33");
        backend.setTtl("200");

        Map<String, String> nameServers = new LinkedHashMap<>();
        nameServers.put("ns1.nip.io.test", "127.0.0.34");
        nameServers.put("ns2.nip.io.test", "127.0.0.35");
        backend.setNameServers(nameServers);

        backend.setDomain("nip.io.test");

        List<String> whitelistedRanges = new ArrayList<>();
        whitelistedRanges.add("127.0.0.0/8"); // This allows us to test that the blacklist works even when the IPs are part of whitelisted ranges
        whitelistedRanges.add("222.173.190.239/32"); // This range covers deadbeef
        backend.setWhitelistedRanges(whitelistedRanges);

        backend.setBlacklistedIps(Collections.singletonList("127.0.0.2"));

        return backend;
    }

    static DynamicBackend configureBackend() {
        return configureBackend("src/test/resources/backend_test.conf");
    }
    
    static DynamicBackend configureBackend(String filename) {
        DynamicBackend backend = new DynamicBackend();
        backend.configure(getTestConfigFilename(filename));
        return backend;
    }
    
    static String getTestConfigFilename(String filename) {
        return System.getProperty("user.dir") + "/" + filename;
    }
}
