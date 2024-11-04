import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Tx {
    public int version;
    public long inCount;
    public List<txInput> inputs;
    public long outCount;
    public List<txOutput> outputs;
    public int lockTime;
    public int seq;

    public Tx(InputStream blockchain) throws IOException {
        this.version = BlockTools.uint4(blockchain);
        this.inCount = BlockTools.varint(blockchain);
        this.inputs = new ArrayList<>();

        for (int i = 0; i < this.inCount; i++) {
            txInput input = new txInput(blockchain);
            this.inputs.add(input);
        }

        this.outCount = BlockTools.varint(blockchain);
        this.outputs = new ArrayList<>();
        if (this.outCount > 0) {
            for (int i = 0; i < this.outCount; i++) {
                txOutput output = new txOutput(blockchain);
                this.outputs.add(output);
            }
        }

        this.lockTime = BlockTools.uint4(blockchain);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("=".repeat(20)).append(" No. ").append(this.seq).append(" Transaction ").append("=".repeat(20)).append("\n");
        sb.append("Tx Version:\t ").append(this.version).append("\n");
        sb.append("Inputs:\t\t ").append(this.inCount).append("\n");

        for (txInput input : this.inputs) {
            sb.append(input.toString());
        }

        sb.append("Outputs:\t ").append(this.outCount).append("\n");
        for (txOutput output : this.outputs) {
            sb.append(output.toString());
        }

        sb.append("Lock Time:\t ").append(this.lockTime);
        return sb.toString();
    }
}
