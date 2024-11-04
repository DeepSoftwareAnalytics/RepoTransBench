import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateTestCase {

    private static <T> List<Pair<T, Boolean>> safeLookahead(Iterable<T> iterable) {
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

    @Test
    public void testLookahead() {
        List<Integer> iterable = Arrays.asList(1, 2, 3);
        List<Pair<Integer, Boolean>> result = safeLookahead(iterable);
        List<Pair<Integer, Boolean>> expected = Arrays.asList(
                new Pair<>(1, true),
                new Pair<>(2, true),
                new Pair<>(3, false)
        );
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadEmpty() {
        List<Integer> iterable = Collections.emptyList();
        List<Pair<Integer, Boolean>> result = safeLookahead(iterable);
        List<Pair<Integer, Boolean>> expected = Collections.emptyList();
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadSingleElement() {
        List<Integer> iterable = Collections.singletonList(42);
        List<Pair<Integer, Boolean>> result = safeLookahead(iterable);
        List<Pair<Integer, Boolean>> expected = Collections.singletonList(new Pair<>(42, false));
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadStrings() {
        List<String> iterable = Arrays.asList("a", "b", "c");
        List<Pair<String, Boolean>> result = safeLookahead(iterable);
        List<Pair<String, Boolean>> expected = Arrays.asList(
                new Pair<>("a", true),
                new Pair<>("b", true),
                new Pair<>("c", false)
        );
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadMixedTypes() {
        List<Object> iterable = Arrays.asList(1, "a", 3.14);
        List<Pair<Object, Boolean>> result = safeLookahead(iterable);
        List<Pair<Object, Boolean>> expected = Arrays.asList(
                new Pair<>(1, true),
                new Pair<>("a", true),
                new Pair<>(3.14, false)
        );
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadLargeIterable() {
        List<Integer> iterable = Stream.iterate(0, n -> n + 1).limit(1000).collect(Collectors.toList());
        List<Pair<Integer, Boolean>> result = safeLookahead(iterable);
        List<Pair<Integer, Boolean>> expected = Stream.iterate(0, n -> n + 1).limit(999)
                .map(i -> new Pair<>(i, true))
                .collect(Collectors.toList());
        expected.add(new Pair<>(999, false));
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadWithNone() {
        List<Object> iterable = Arrays.asList(null, 1, "a");
        List<Pair<Object, Boolean>> result = safeLookahead(iterable);
        List<Pair<Object, Boolean>> expected = Arrays.asList(
                new Pair<>(null, true),
                new Pair<>(1, true),
                new Pair<>("a", false)
        );
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadNestedIterable() {
        List<List<Integer>> iterable = Arrays.asList(
                Arrays.asList(1, 2),
                Arrays.asList(3, 4),
                Arrays.asList(5, 6)
        );
        List<Pair<List<Integer>, Boolean>> result = safeLookahead(iterable);
        List<Pair<List<Integer>, Boolean>> expected = Arrays.asList(
                new Pair<>(Arrays.asList(1, 2), true),
                new Pair<>(Arrays.asList(3, 4), true),
                new Pair<>(Arrays.asList(5, 6), false)
        );
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadWithDict() {
        List<Map<String, Integer>> iterable = Arrays.asList(
                Collections.singletonMap("a", 1),
                Collections.singletonMap("b", 2),
                Collections.singletonMap("c", 3)
        );
        List<Pair<Map<String, Integer>, Boolean>> result = safeLookahead(iterable);
        List<Pair<Map<String, Integer>, Boolean>> expected = Arrays.asList(
                new Pair<>(Collections.singletonMap("a", 1), true),
                new Pair<>(Collections.singletonMap("b", 2), true),
                new Pair<>(Collections.singletonMap("c", 3), false)
        );
        assertEquals(expected, result);
    }

    @Test
    public void testLookaheadWithTuples() {
        List<Pair<Integer, String>> iterable = Arrays.asList(
                new Pair<>(1, "a"),
                new Pair<>(2, "b"),
                new Pair<>(3, "c")
        );
        List<Pair<Pair<Integer, String>, Boolean>> result = safeLookahead(iterable);
        List<Pair<Pair<Integer, String>, Boolean>> expected = Arrays.asList(
                new Pair<>(new Pair<>(1, "a"), true),
                new Pair<>(new Pair<>(2, "b"), true),
                new Pair<>(new Pair<>(3, "c"), false)
        );
        assertEquals(expected, result);
    }

    @Test
    public void testCreateDockerfile() {
        String baseOs = "ubuntu:20.04";
        String title = "Test Dockerfile";
        String author = "Test Author";
        List<String> env = Arrays.asList("ENV_VAR=value");
        List<String> deps = Arrays.asList("curl", "wget");
        String blenderDownloadUrl = "https://example.com/blender.tar.xz";
        String pythonDownloadUrl = "https://example.com/python.tgz";
        String pythonVersion = "3.9";
        String workdir = "/app";

        String dockerfile = createDockerfile(
                baseOs, title, author, env, deps, blenderDownloadUrl, pythonDownloadUrl, pythonVersion, workdir
        );

        assertTrue(dockerfile.contains("FROM ubuntu:20.04"));
        assertTrue(dockerfile.contains("LABEL Author=\"Test Author\""));
        assertTrue(dockerfile.contains("LABEL Title=\"Test Dockerfile\""));
        assertTrue(dockerfile.contains("ENV ENV_VAR=value"));
        assertTrue(dockerfile.contains("RUN apt-get update && apt-get install -y \\\n\tcurl \\ \n\twget"));
        assertTrue(dockerfile.contains("RUN wget https://example.com/blender.tar.xz"));
        assertTrue(dockerfile.contains("RUN wget https://example.com/python.tgz"));
        assertTrue(dockerfile.contains("WORKDIR /app"));
    }

    @Test
    public void testCreateDockerfileNoDeps() {
        String baseOs = "ubuntu:20.04";
        String title = "Test Dockerfile";
        String author = "Test Author";
        List<String> env = Arrays.asList("ENV_VAR=value");
        List<String> deps = Collections.emptyList();
        String blenderDownloadUrl = "https://example.com/blender.tar.xz";
        String pythonDownloadUrl = "https://example.com/python.tgz";
        String pythonVersion = "3.9";
        String workdir = "/app";

        String dockerfile = createDockerfile(
                baseOs, title, author, env, deps, blenderDownloadUrl, pythonDownloadUrl, pythonVersion, workdir
        );

        assertTrue(dockerfile.contains("FROM ubuntu:20.04"));
        assertTrue(dockerfile.contains("LABEL Author=\"Test Author\""));
        assertTrue(dockerfile.contains("LABEL Title=\"Test Dockerfile\""));
        assertTrue(dockerfile.contains("ENV ENV_VAR=value"));
        assertFalse(dockerfile.contains("RUN apt-get update && apt-get install -y \\\n\tcurl \\ \n\twget"));
        assertTrue(dockerfile.contains("RUN wget https://example.com/blender.tar.xz"));
        assertTrue(dockerfile.contains("RUN wget https://example.com/python.tgz"));
        assertTrue(dockerfile.contains("WORKDIR /app"));
    }

    @Test
    public void testCreateDockerfileNoEnv() {
        String baseOs = "ubuntu:20.04";
        String title = "Test Dockerfile";
        String author = "Test Author";
        List<String> env = Collections.emptyList();
        List<String> deps = Arrays.asList("curl", "wget");
        String blenderDownloadUrl = "https://example.com/blender.tar.xz";
        String pythonDownloadUrl = "https://example.com/python.tgz";
        String pythonVersion = "3.9";
        String workdir = "/app";

        String dockerfile = createDockerfile(
                baseOs, title, author, env, deps, blenderDownloadUrl, pythonDownloadUrl, pythonVersion, workdir
        );

        assertTrue(dockerfile.contains("FROM ubuntu:20.04"));
        assertTrue(dockerfile.contains("LABEL Author=\"Test Author\""));
        assertTrue(dockerfile.contains("LABEL Title=\"Test Dockerfile\""));
        assertFalse(dockerfile.contains("ENV"));
        assertTrue(dockerfile.contains("RUN apt-get update && apt-get install -y \\\n\tcurl \\ \n\twget"));
        assertTrue(dockerfile.contains("RUN wget https://example.com/blender.tar.xz"));
        assertTrue(dockerfile.contains("RUN wget https://example.com/python.tgz"));
        assertTrue(dockerfile.contains("WORKDIR /app"));
    }

    @Test
    public void testCreateDockerfileDifferentBaseOs() {
        String baseOs = "debian:latest";
        String title = "Debian Dockerfile";
        String author = "Debian Author";
        List<String> env = Arrays.asList("ENV_VAR=value");
        List<String> deps = Arrays.asList("curl", "wget");
        String blenderDownloadUrl = "https://example.com/blender.tar.xz";
        String pythonDownloadUrl = "https://example.com/python.tgz";
        String pythonVersion = "3.9";
        String workdir = "/app";

        String dockerfile = createDockerfile(
                baseOs, title, author, env, deps, blenderDownloadUrl, pythonDownloadUrl, pythonVersion, workdir
        );

        assertTrue(dockerfile.contains("FROM debian:latest"));
        assertTrue(dockerfile.contains("LABEL Author=\"Debian Author\""));
        assertTrue(dockerfile.contains("LABEL Title=\"Debian Dockerfile\""));
        assertTrue(dockerfile.contains("ENV ENV_VAR=value"));
        assertTrue(dockerfile.contains("RUN apt-get update && apt-get install -y \\\n\tcurl \\ \n\twget"));
        assertTrue(dockerfile.contains("RUN wget https://example.com/blender.tar.xz"));
        assertTrue(dockerfile.contains("RUN wget https://example.com/python.tgz"));
        assertTrue(dockerfile.contains("WORKDIR /app"));
    }

    // Mock createDockerfile method for testing
    private String createDockerfile(String baseOs, String title, String author, List<String> env, List<String> deps,
                                    String blenderDownloadUrl, String pythonDownloadUrl, String pythonVersion, String workdir) {
        StringBuilder dockerfile = new StringBuilder();
        dockerfile.append("FROM ").append(baseOs).append("\n");
        dockerfile.append("LABEL Author=\"").append(author).append("\"\n");
        dockerfile.append("LABEL Title=\"").append(title).append("\"\n");

        for (String envVar : env) {
            dockerfile.append("ENV ").append(envVar).append("\n");
        }

        if (!deps.isEmpty()) {
            dockerfile.append("RUN apt-get update && apt-get install -y \\\n");
            for (String dep : deps) {
                dockerfile.append("\t").append(dep).append(" \\ \n");
            }
            dockerfile.setLength(dockerfile.length() - 3); // Remove last " \\ \n"
            dockerfile.append("\n");
        }

        dockerfile.append("RUN wget ").append(blenderDownloadUrl).append("\n");
        dockerfile.append("RUN wget ").append(pythonDownloadUrl).append("\n");
        dockerfile.append("WORKDIR ").append(workdir).append("\n");

        return dockerfile.toString();
    }
}
