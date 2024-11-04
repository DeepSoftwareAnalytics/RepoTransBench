import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Block {
    private boolean continueParsing = true;
    private int magicNum;
    private int blocksize;
    private BlockHeader blockheader;
    private long txCount;
    private List<Tx> Txs;

    public Block(InputStream blockchain) throws IOException {
        if (this.hasLength(blockchain, 8)) {
            this.magicNum = BlockTools.uint4(blockchain);
            this.blocksize = BlockTools.uint4(blockchain);
        } else {
            this.continueParsing = false;
            return;
        }

        if (this.hasLength(blockchain, this.blocksize)) {
            this.setHeader(blockchain);
            this.txCount = BlockTools.varint(blockchain);
            this.Txs = new ArrayList<>();

            for (int i = 0; i < this.txCount; i++) {
                Tx tx = new Tx(blockchain);
                tx.seq = i;
                this.Txs.add(tx);
            }
        } else {
            this.continueParsing = false;
        }
    }

    public boolean continueParsing() {
        return this.continueParsing;
    }

    public int getBlocksize() {
        return this.blocksize;
    }

    public boolean hasLength(InputStream blockchain, int size) throws IOException {
        blockchain.mark(size);
        byte[] buffer = new byte[size];
        int bytesRead = blockchain.read(buffer);
        blockchain.reset();
        return bytesRead == size;
    }

    public void setHeader(InputStream blockchain) throws IOException {
        this.blockheader = new BlockHeader(blockchain);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("Magic No: \t").append(String.format("%8x", this.magicNum)).append("\n");
        sb.append("Blocksize: \t").append(this.blocksize).append("\n");
        sb.append("#".repeat(10)).append(" Block Header ").append("#".repeat(10)).append("\n");
        sb.append(this.blockheader.toString()).append("\n");
        sb.append("##### Tx Count: ").append(this.txCount).append("\n");
        for (Tx t : this.Txs) {
            sb.append(t.toString());
        }
        sb.append("#### end of all ").append(this.txCount).append(" transactions");
        return sb.toString();
    }
}
