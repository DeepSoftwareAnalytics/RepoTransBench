import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.Map;

public class Pyaml {

    public static String dump(Map<String, Object> data) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setWidth(100);

        Representer representer = new Representer();
        Yaml yaml = new Yaml(new Constructor(), representer, options);

        StringWriter writer = new StringWriter();
        yaml.dump(data, writer);
        return writer.toString();
    }

    public static void main(String[] args) {
        // Example usage
        Map<String, Object> data = Map.of(
                "key", "value",
                "path", "/some/path"
        );
        String yamlStr = dump(data);
        System.out.println(yamlStr);
    }
}
