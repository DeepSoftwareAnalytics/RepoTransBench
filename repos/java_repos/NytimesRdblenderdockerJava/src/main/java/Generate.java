import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generate {

    public static class DockerImage {
        String baseOsImage;
        String tag;
        String blenderDownloadUrl;
        String pythonDownloadUrl;
        String pythonVersion;
        List<String> env;

        public DockerImage(String baseOsImage, String tag, String blenderDownloadUrl, String pythonDownloadUrl,
                            String pythonVersion, List<String> env) {
            this.baseOsImage = baseOsImage;
            this.tag = tag;
            this.blenderDownloadUrl = blenderDownloadUrl;
            this.pythonDownloadUrl = pythonDownloadUrl;
            this.pythonVersion = pythonVersion;
            this.env = env;
        }
    }

    public static class Manifest {
        String title;
        String author;
        List<String> env;
        List<String> deps;
        List<DockerImage> images;

        public Manifest(String title, String author, List<String> env, List<String> deps, List<DockerImage> images) {
            this.title = title;
            this.author = author;
            this.env = env;
            this.deps = deps;
            this.images = images;
        }
    }

    public static <T> List<Pair<T, Boolean>> lookahead(List<T> iterable) {
        List<Pair<T, Boolean>> result = new ArrayList<>();
        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            T last = iterator.next();
            while (iterator.hasNext()) {
                T current = iterator.next();
                result.add(new Pair<>(last, true));
                last = current;
            }
            result.add(new Pair<>(last, false));
        }
        return result;
    }

    public static String createDockerfile(String baseOs, String title, String author, List<String> env, List<String> deps,
                                          String blenderDownloadUrl, String pythonDownloadUrl, String pythonVersion,
                                          String workdir) {

        StringBuilder dockerfile = new StringBuilder();
        dockerfile.append("# Dockerfile autogenerated on ").append(new Date()).append(" by ").append(System.getProperty("user.name")).append("\n");
        dockerfile.append("# Please do not edit this file directly\n\n");

        dockerfile.append("FROM ").append(baseOs).append("\n\n");
        dockerfile.append("LABEL Author=\"").append(author).append("\"\n");
        dockerfile.append("LABEL Title=\"").append(title).append("\"\n\n");

        dockerfile.append("# Environment variables\n");
        for (String envVar : env) {
            dockerfile.append("ENV ").append(envVar).append("\n");
        }
        dockerfile.append("\n");

        dockerfile.append("# Install dependencies\n");
        dockerfile.append("RUN apt-get update && apt-get install -y \\\n");

        String archivetype = blenderDownloadUrl.substring(blenderDownloadUrl.lastIndexOf('.') + 1);
        String archiveflags = "xjvf";

        if ("xz".equals(archivetype)) {
            archiveflags = "xvf";
            deps.add("xz-utils");
        }

        List<Pair<String, Boolean>> dependenciesWithState = lookahead(deps);
        for (Pair<String, Boolean> entry : dependenciesWithState) {
            dockerfile.append("\t").append(entry.getFirst());
            if (entry.getSecond()) {
                dockerfile.append(" \\");
            }
            dockerfile.append("\n");
        }

        if ("xz".equals(archivetype)) {
            deps.remove(deps.size() - 1);
        }

        dockerfile.append("\n");

        dockerfile.append("# Download and install Blender\n");
        dockerfile.append("RUN wget ").append(blenderDownloadUrl).append(" \\\n");
        dockerfile.append("\t&& tar -").append(archiveflags).append(" ").append(blenderDownloadUrl.substring(blenderDownloadUrl.lastIndexOf('/') + 1)).append(" --strip-components=1 -C /bin \\\n");
        dockerfile.append("\t&& rm -rf ").append(blenderDownloadUrl.substring(blenderDownloadUrl.lastIndexOf('/') + 1)).append(" \\\n");
        dockerfile.append("\t&& rm -rf ").append(blenderDownloadUrl.substring(blenderDownloadUrl.lastIndexOf('/') + 1).split(".tar.")[0]).append("\n\n");

        dockerfile.append("# Download the Python source since it is not bundled with Blender\n");
        dockerfile.append("RUN wget ").append(pythonDownloadUrl).append(" \\\n");
        dockerfile.append("\t&& tar -xzf ").append(pythonDownloadUrl.substring(pythonDownloadUrl.lastIndexOf('/') + 1)).append(" \\\n");
        dockerfile.append("\t&& cp -r ").append(pythonDownloadUrl.substring(pythonDownloadUrl.lastIndexOf('/') + 1).split(".tgz")[0]).append("/Include/* $BLENDER_PATH/python/include/").append(pythonVersion).append(" \\\n");
        dockerfile.append("\t&& rm -rf ").append(pythonDownloadUrl.substring(pythonDownloadUrl.lastIndexOf('/') + 1)).append(" \\\n");
        dockerfile.append("\t&& rm -rf ").append(pythonDownloadUrl.substring(pythonDownloadUrl.lastIndexOf('/') + 1).split(".tgz")[0]).append("\n\n");

        dockerfile.append("# Blender comes with a super outdated version of numpy (which is needed for matplotlib / opencv) so override it with a modern one\n");
        dockerfile.append("RUN rm -rf $BLENDER_PATH/python/lib/").append(pythonVersion).append("/site-packages/numpy\n\n");

        dockerfile.append("# Must first ensurepip to install Blender pip3 and then new numpy\n");
        dockerfile.append("RUN $BLENDERPY -m ensurepip && $BLENDERPIP install --upgrade pip && $BLENDERPIP install numpy\n\n");

        dockerfile.append("# Set the working directory\n");
        dockerfile.append("WORKDIR ").append(workdir);

        return dockerfile.toString();
    }

    public static void main(String[] args) throws IOException {
        String outputFolder = "./dist/";
        Path manifestPath = Paths.get("./manifest.json");

        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(manifestPath)) {
            Manifest manifest = gson.fromJson(reader, Manifest.class);

            for (DockerImage image : manifest.images) {
                Path imagePath = Paths.get(outputFolder, image.tag);
                if (Files.notExists(imagePath)) {
                    Files.createDirectories(imagePath);
                }

                String dockerfile = createDockerfile(
                        image.baseOsImage,
                        manifest.title,
                        manifest.author,
                        Stream.concat(manifest.env.stream(), image.env.stream()).collect(Collectors.toList()),
                        manifest.deps,
                        image.blenderDownloadUrl,
                        image.pythonDownloadUrl,
                        image.pythonVersion,
                        "/"
                );

                Path dockerfilePath = imagePath.resolve("Dockerfile");
                Files.writeString(dockerfilePath, dockerfile);
            }
        }
    }
}
