import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Client {

    private static final String API_URL = "https://pdftables.com/api";
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 300000;

    private String apiKey;
    private String apiUrl;
    private int timeout;

    static final Map<String, String> FORMATS_EXT = new HashMap<>();
    static final Map<String, String> EXT_FORMATS = new HashMap<>();
    static {
        FORMATS_EXT.put("csv", ".csv");
        FORMATS_EXT.put("html", ".html");
        FORMATS_EXT.put("xlsx-multiple", ".xlsx");
        FORMATS_EXT.put("xlsx-single", ".xlsx");
        FORMATS_EXT.put("xml", ".xml");

        EXT_FORMATS.put(".csv", "csv");
        EXT_FORMATS.put(".html", "html");
        EXT_FORMATS.put(".xlsx", "xlsx-multiple");
        EXT_FORMATS.put(".xml", "xml");
    }

    public Client(String apiKey) {
        this(apiKey, API_URL, CONNECT_TIMEOUT + READ_TIMEOUT);
    }

    public Client(String apiKey, String apiUrl) {
        this(apiKey, apiUrl, CONNECT_TIMEOUT + READ_TIMEOUT);
    }

    public Client(String apiKey, String apiUrl, int timeout) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.timeout = timeout;
    }

    public byte[] convert(String pdfPath, String outPath) throws IOException, APIException {
        return convert(pdfPath, outPath, null);
    }

    public byte[] convert(String pdfPath, String outPath, String outFormat) throws IOException, APIException {
        return convert(pdfPath, outPath, outFormat, null);
    }

    public byte[] convert(String pdfPath, String outPath, String outFormat, Map<String, String> queryParams)
            throws IOException, APIException {
        String[] formatAndPath = ensureFormatExt(outPath, outFormat);
        outPath = formatAndPath[0];
        outFormat = formatAndPath[1];

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             FileInputStream pdfInputStream = new FileInputStream(pdfPath)) {

            HttpPost httpPost = new HttpPost(apiUrl);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addPart("f", new FileBody(new File(pdfPath)));
            entityBuilder.addPart("key", new StringBody(apiKey));
            entityBuilder.addPart("format", new StringBody(outFormat));

            if (queryParams != null) {
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    entityBuilder.addPart(entry.getKey(), new StringBody(entry.getValue()));
                }
            }

            httpPost.setEntity(entityBuilder.build());

            HttpResponse response = httpClient.execute(httpPost);
            checkResponse(response);

            if (outPath == null) {
                return response.getEntity().getContent().readAllBytes();
            } else {
                try (BufferedOutputStream outFileStream = new BufferedOutputStream(new FileOutputStream(outPath))) {
                    response.getEntity().writeTo(outFileStream);
                    outFileStream.flush();
                }
                return null;
            }
        }
    }

    public int remaining() throws IOException, APIException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(apiUrl + "/remaining?key=" + apiKey);
            HttpResponse response = httpClient.execute(httpGet);
            checkResponse(response);
            return Integer.parseInt(new String(response.getEntity().getContent().readAllBytes()));
        }
    }

    private void checkResponse(HttpResponse response) throws APIException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_BAD_REQUEST) {
            throw new APIException("Unknown file format");
        } else if (status == HttpStatus.SC_UNAUTHORIZED) {
            throw new APIException("Unauthorized API key");
        } else if (status == HttpStatus.SC_PAYMENT_REQUIRED) {
            throw new APIException("Usage limit exceeded");
        } else if (status == HttpStatus.SC_FORBIDDEN) {
            throw new APIException("Unknown format requested");
        } else if (status != HttpStatus.SC_OK) {
            throw new APIException("Returned an error: " + status);
        }
    }

    public static String[] ensureFormatExt(String outPath, String outFormat) {
        if (outFormat != null && !FORMATS_EXT.containsKey(outFormat)) {
            throw new IllegalArgumentException("Invalid output format");
        }

        String defaultFormat = "xlsx-multiple";

        if (outPath == null) {
            if (outFormat == null) {
                outFormat = defaultFormat;
            }
            return new String[]{null, outFormat};
        }

        int lastDotIndex = outPath.lastIndexOf('.');
        String ext = (lastDotIndex == -1) ? "" : outPath.substring(lastDotIndex);

        if (outFormat == null) {
            if (EXT_FORMATS.containsKey(ext)) {
                outFormat = EXT_FORMATS.get(ext);
            } else {
                outFormat = defaultFormat;
            }
        }

        if (!FORMATS_EXT.get(outFormat).equals(ext)) {
            outPath = outPath + FORMATS_EXT.get(outFormat);
        }

        return new String[]{outPath, outFormat};
    }
}

