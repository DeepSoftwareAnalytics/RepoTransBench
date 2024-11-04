import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HackerTargetApiTest {

    @Test
    public void testTracerouteScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(1, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(1, "facebook.com");
    }

    @Test
    public void testPingScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(2, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(2, "facebook.com");
    }

    @Test
    public void testDnsLookupScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(3, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(3, "facebook.com");
    }

    @Test
    public void testReverseDnsScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(4, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(4, "facebook.com");
    }

    @Test
    public void testFindDnsHostScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(5, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(5, "facebook.com");
    }

    @Test
    public void testFindSharedDnsScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(6, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(6, "facebook.com");
    }

    @Test
    public void testZoneTransferScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(7, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(7, "facebook.com");
    }

    @Test
    public void testWhoisLookupScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(8, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(8, "facebook.com");
    }

    @Test
    public void testIpLocationLookupScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(9, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(9, "facebook.com");
    }

    @Test
    public void testReverseIpLookupScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(10, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(10, "facebook.com");
    }

    @Test
    public void testTcpPortScanLookupScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(11, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(11, "facebook.com");
    }

    @Test
    public void testSubnetLookupScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(12, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(12, "facebook.com");
    }

    @Test
    public void testHttpHeaderCheckScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(13, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(13, "facebook.com");
    }

    @Test
    public void testExtractPageLinksScript() {
        HackerTargetApi mockApi = mock(HackerTargetApi.class);
        mockApi.hackertargetApi(14, "facebook.com");
        verify(mockApi, times(1)).hackertargetApi(14, "facebook.com");
    }
}
