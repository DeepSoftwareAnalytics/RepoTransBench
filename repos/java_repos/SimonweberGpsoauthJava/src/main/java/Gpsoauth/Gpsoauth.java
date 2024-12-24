package Gpsoauth;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import javax.net.ssl.*;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class Gpsoauth {
    private static final String AUTH_URL = "https://android.clients.google.com/auth";
    private static final String USER_AGENT = "GoogleAuth/1.4";
    private static final PublicKey ANDROID_KEY_7_3_29;

    static {
        try {
            ANDROID_KEY_7_3_29 = Google.keyFromB64("<Put Base64 Key Here>");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static CloseableHttpClient createHttpClient() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }}, new java.security.SecureRandom());

        return HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .build();
    }

    private static Map<String, String> performAuthRequest(Map<String, Object> data, Map<String, String> proxies) throws Exception {
        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpPost post = new HttpPost(AUTH_URL);
            post.setHeader("Accept-Encoding", "identity");
            post.setHeader("Content-type", "application/x-www-form-urlencoded");
            post.setHeader("User-Agent", USER_AGENT);

            if (proxies != null) {
                for (Map.Entry<String, String> entry : proxies.entrySet()) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            }

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue().toString());
            }

            StringEntity entity = new StringEntity(sb.toString());
            post.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                HttpEntity resEntity = response.getEntity();
                String responseContent = EntityUtils.toString(resEntity);
                return Google.parseAuthResponse(responseContent);
            }
        }
    }

    public static Map<String, String> performMasterLogin(String email, String password, String androidId, String service, String deviceCountry, String operatorCountry, String lang, int sdkVersion, Map<String, String> proxies, String clientSig) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("accountType", "HOSTED_OR_GOOGLE");
        data.put("Email", email);
        data.put("has_permission", 1);
        data.put("add_account", 1);
        data.put("EncryptedPasswd", Google.constructSignature(email, password, ANDROID_KEY_7_3_29));
        data.put("service", service);
        data.put("source", "android");
        data.put("androidId", androidId);
        data.put("device_country", deviceCountry);
        data.put("operatorCountry", operatorCountry);
        data.put("lang", lang);
        data.put("sdk_version", sdkVersion);
        data.put("client_sig", clientSig);
        data.put("callerSig", clientSig);
        data.put("droidguard_results", "dummy123");

        return performAuthRequest(data, proxies);
    }

    public static Map<String, String> exchangeToken(String email, String token, String androidId, String service, String deviceCountry, String operatorCountry, String lang, int sdkVersion, Map<String, String> proxies, String clientSig) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("accountType", "HOSTED_OR_GOOGLE");
        data.put("Email", email);
        data.put("has_permission", 1);
        data.put("add_account", 1);
        data.put("ACCESS_TOKEN", 1);
        data.put("Token", token);
        data.put("service", service);
        data.put("source", "android");
        data.put("androidId", androidId);
        data.put("device_country", deviceCountry);
        data.put("operatorCountry", operatorCountry);
        data.put("lang", lang);
        data.put("sdk_version", sdkVersion);
        data.put("client_sig", clientSig);
        data.put("callerSig", clientSig);
        data.put("droidguard_results", "dummy123");

        return performAuthRequest(data, proxies);
    }

    public static Map<String, String> performOauth(String email, String masterToken, String androidId, String service, String app, String clientSig, String deviceCountry, String operatorCountry, String lang, int sdkVersion, Map<String, String> proxies) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("accountType", "HOSTED_OR_GOOGLE");
        data.put("Email", email);
        data.put("has_permission", 1);
        data.put("EncryptedPasswd", masterToken);
        data.put("service", service);
        data.put("source", "android");
        data.put("androidId", androidId);
        data.put("app", app);
        data.put("client_sig", clientSig);
        data.put("device_country", deviceCountry);
        data.put("operatorCountry", operatorCountry);
        data.put("lang", lang);
        data.put("sdk_version", sdkVersion);

        return performAuthRequest(data, proxies);
    }
}
