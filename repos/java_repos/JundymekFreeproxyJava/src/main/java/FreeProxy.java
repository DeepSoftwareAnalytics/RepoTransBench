import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FreeProxy {
    private final List<String> countryIds;
    private final double timeout;
    private final boolean randomize;
    private final boolean anonym;
    private final boolean elite;
    private final Boolean google;
    private final String schema;

    public FreeProxy() {
        this(null, 0.5, false, false, false, null, false);
    }

    public FreeProxy(List<String> countryIds, double timeout, boolean randomize, boolean anonym, boolean elite, Boolean google, boolean https) {
        this.countryIds = countryIds;
        this.timeout = timeout;
        this.randomize = randomize;
        this.anonym = anonym;
        this.elite = elite;
        this.google = google;
        this.schema = https ? "https" : "http";
    }

    public List<String> getProxyList(boolean repeat) throws FreeProxyException {
        try {
            Document doc = Jsoup.connect(getWebsite(repeat)).timeout((int) (timeout * 1000)).get();
            Elements rows = doc.select("table#proxylisttable tr");
            List<String> proxies = new ArrayList<>();
            for (Element row : rows) {
                if (criteria(row)) {
                    proxies.add(row.child(0).text() + ":" + row.child(1).text());
                }
            }
            return proxies;
        } catch (IOException e) {
            throw new FreeProxyException("Request to " + getWebsite(repeat) + " failed", e);
        }
    }

    protected String getWebsite(boolean repeat) {
        if (repeat) {
            return "https://free-proxy-list.net";
        } else if (countryIds != null && countryIds.contains("US")) {
            return "https://www.us-proxy.org";
        } else if (countryIds != null && countryIds.contains("GB")) {
            return "https://free-proxy-list.net/uk-proxy.html";
        } else {
            return "https://www.sslproxies.org";
        }
    }

    protected boolean criteria(Element row) {
        boolean countryCriteria = countryIds == null || countryIds.contains(row.child(2).text());
        boolean eliteCriteria = !elite || row.child(4).text().contains("elite");
        boolean anonymCriteria = !anonym || ("anonymous".equals(row.child(4).text()));
        boolean googleCriteria = google == null || google.equals("yes".equals(row.child(5).text()));
        boolean httpsCriteria = "http".equals(schema) || "yes".equalsIgnoreCase(row.child(6).text());
        return countryCriteria && eliteCriteria && anonymCriteria && googleCriteria && httpsCriteria;
    }

    public String get(boolean repeat) throws FreeProxyException {
        List<String> proxyList = getProxyList(repeat);
        if (randomize) {
            Collections.shuffle(proxyList, new Random());
        }
        String workingProxy = null;
        for (String proxyAddress : proxyList) {
            try {
                workingProxy = checkIfProxyIsWorking(proxyAddress);
                if (workingProxy != null) {
                    return workingProxy;
                }
            } catch (IOException ignored) {
            }
        }
        if (workingProxy == null && !repeat) {
            return get(true);
        }
        throw new FreeProxyException("There are no working proxies at this time.");
    }

    private String checkIfProxyIsWorking(String proxyAddress) throws IOException {
        String[] split = proxyAddress.split(":");
        String ip = split[0];

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(schema + "://www.google.com");
            request.setConfig(org.apache.http.client.config.RequestConfig.custom().setSocketTimeout((int) (timeout * 1000)).setConnectTimeout((int) (timeout * 1000)).setConnectionRequestTimeout((int) (timeout * 1000)).build());
            request.setHeader("User-Agent", "Mozilla/5.0");
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                return schema + "://" + proxyAddress;
            }
        } catch (HttpResponseException e) {
            return null;
        }
        return null;
    }
}

