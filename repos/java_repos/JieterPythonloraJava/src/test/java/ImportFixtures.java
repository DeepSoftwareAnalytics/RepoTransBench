import lora.LoRaPayload;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.jdom2.JDOMException;
import java.io.IOException;

public class ImportFixtures {

    public static void main(String[] args) throws JDOMException, IOException {
        // 获取命令行参数
        String devAddr = (args.length > 0) ? args[0] : "14000122";
        String fixtureFilename = devAddr + ".txt";

        // 检查文件是否存在
        if (!Files.exists(Paths.get(fixtureFilename))) {
            System.out.println("File containing fixtures does not exist (" + fixtureFilename + ")");
            System.exit(1);
        }

        try {
            // 打开文件读取数据
            List<String> lines = Files.readAllLines(Paths.get(fixtureFilename));
            String filenameFmt = "fixtures/" + devAddr + "/payload_%s-%d.xml";
            String loraXmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DevEUI_uplink xmlns=\"http://uri.actility.com/lora\">";
            int enc = 1;
            int plain = 1;

            for (String item : lines) {
                item = item.trim();

                // 跳过不包含 XML 头或注释的短行
                if (!item.startsWith(loraXmlHeader)) {
                    continue;
                }

                // 创建 LoRaPayload 对象
                LoRaPayload payload = new LoRaPayload(item);

                String filename;
                if ("1".equals(payload.getAttribute("FPort"))) {
                    // 未加密
                    filename = String.format(filenameFmt, "plaintext", plain);
                    plain++;
                } else {
                    // 加密
                    filename = String.format(filenameFmt, "encrypted", enc);
                    enc++;
                    // 创建对应的空文本文件用于保存解密后的内容
                    Files.write(Paths.get(filename.replace(".xml", ".txt")), new byte[0]);
                }

                // 写入XML文件
                Files.write(Paths.get(filename), item.replace("><", ">\n<").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
