import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Build {

    public static class DockerImage {
        String tag;

        public DockerImage(String tag) {
            this.tag = tag;
        }
    }

    public static class Manifest {
        List<DockerImage> images;

        public Manifest(List<DockerImage> images) {
            this.images = images;
        }
    }

    public static void main(String[] args) throws IOException {
        String outputFolder = "./dist/";
        Path manifestPath = Paths.get("./manifest.json");

        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(manifestPath)) {
            Manifest manifest = gson.fromJson(reader, Manifest.class);

            for (DockerImage image : manifest.images) {
                Path imagePath = Paths.get(outputFolder, image.tag);
                if (Files.exists(imagePath)) {
                    System.out.println("----------------------------------------------------------");
                    System.out.println("🛠️ Building image nytimes/blender:" + image.tag);

                    // Build the Docker image
                    new ProcessBuilder()
                            .command("docker", "build", "-q", "./", "-f", outputFolder + image.tag + "/Dockerfile", "-t", "nytimes/blender:" + image.tag)
                            .inheritIO()
                            .start()
                            .waitFor();

                    // Copy the README from root so it deploys to DockerHub
                    if (System.getenv("CI") != null) {
                        System.out.println("Copying README to Dockerfile folder on CI so it deploys to Docker Hub\n");
                        Files.copy(Paths.get("./README.md"), imagePath.resolve("README.md"));
                    }

                    System.out.println("✔️ Built and tagged image nytimes/blender:" + image.tag);
                    System.out.println("----------------------------------------------------------\n");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
