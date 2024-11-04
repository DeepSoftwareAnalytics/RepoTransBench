public class URI {
    private final String uri;

    public URI(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof URI) {
            return uri.equals(((URI) obj).uri);
        }
        return false;
    }

    @Override
    public String toString() {
        return uri;
    }
}
