import java.util.List;

public class Transaction {
    private long ver;
    private List<Input> inputs;
    private List<Output> outputs;
    private long lockTime;
    private String hash;

    // Getters and Setters
    public long getVer() {
        return ver;
    }

    public void setVer(long ver) {
        this.ver = ver;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Output> outputs) {
        this.outputs = outputs;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
