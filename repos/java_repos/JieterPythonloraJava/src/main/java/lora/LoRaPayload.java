package lora;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Namespace;
import org.jdom2.JDOMException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class LoRaPayload {
    private static final String XMLNS = "http://uri.actility.com/lora";
    private static final String WKT_POINT_FMT = "SRID=4326;POINT({lng} {lat})";

    private Element payload;

    public LoRaPayload(String xmlstr) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlstr.getBytes(StandardCharsets.UTF_8));
        this.payload = saxBuilder.build(inputStream).getRootElement();

        if (!this.payload.getName().equals("DevEUI_uplink")) {
            throw new IllegalArgumentException("LoRaPayload expects an XML-string containing a DevEUI_uplink tag as root element");
        }
    }

    public String getAttribute(String name) {
        Namespace ns = Namespace.getNamespace(XMLNS);
        Element element = payload.getChild(name, ns);
        if (element != null) {
            return element.getText();
        } else {
            System.out.println("Could not find tag with name: " + name);
            return null;
        }
    }

    public byte[] decrypt(String key, String devAddr) throws Exception {
        int sequenceCounter = Integer.parseInt(getAttribute("FCntUp"));
        String payloadHex = getAttribute("payload_hex");

        if (payloadHex != null) {
            return Crypto.loramacDecrypt(payloadHex, sequenceCounter, key, devAddr, Crypto.UP_LINK);
        }
        return new byte[0];
    }

    public String getLrrLocation() {
        String lngStr = getAttribute("LrrLON");
        String latStr = getAttribute("LrrLAT");

        if (lngStr != null && latStr != null) {
            double lng = Double.parseDouble(lngStr);
            double lat = Double.parseDouble(latStr);
            return WKT_POINT_FMT.replace("{lng}", Double.toString(lng)).replace("{lat}", Double.toString(lat));
        }

        return null;
    }
}
