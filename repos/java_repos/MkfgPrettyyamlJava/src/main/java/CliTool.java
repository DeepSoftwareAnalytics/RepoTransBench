import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.Map;

public class CliTool {

    public static void main(String[] args) {
        try {
            InputStream inputStream = System.in;
            OutputStream outputStream = System.out;
            if (args.length > 0) {
                inputStream = new FileInputStream(args[0]);
            }

            Yaml yaml = new Yaml(new Constructor(), new Representer());
            Map<String, Object> data = yaml.load(inputStream);

            String yamlStr = Pyaml.dump(data);
            outputStream.write(yamlStr.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
