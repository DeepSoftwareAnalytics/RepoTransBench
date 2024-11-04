public class Partition {
    public String action() {
        return "netem loss 100%";
    }

    public String desc() {
        return "network partition";
    }
}
