public class Event {
    private String id;
    private String event;
    private String data;
    private Integer retry;

    public Event() {
        this.id = null;
        this.event = "message";
        this.data = "";
        this.retry = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(event + " event");
        if (id != null) {
            s.append(" #").append(id);
        }
        if (data != null) {
            s.append(", ").append(data.length()).append(" byte").append(data.length() == 1 ? "" : "s");
        } else {
            s.append(", no data");
        }
        if (retry != null) {
            s.append(", retry in ").append(retry).append("ms");
        }
        return s.toString();
    }
}
