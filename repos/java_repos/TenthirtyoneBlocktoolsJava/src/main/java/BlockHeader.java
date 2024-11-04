import java.io.InputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class BlockHeader {
    public int version;
    public byte[] previousHash;
    public byte[] merkleHash;
    public int time;
    public int bits;
    public int nonce;

    public BlockHeader(InputStream blockchain) throws IOException {
        this.version = BlockTools.uint4(blockchain);
        this.previousHash = BlockTools.hash32(blockchain);
        this.merkleHash = BlockTools.hash32(blockchain);
        this.time = BlockTools.uint4(blockchain);
        this.bits = BlockTools.uint4(blockchain);
        this.nonce = BlockTools.uint4(blockchain);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Version:\t ").append(this.version).append("\n");
        sb.append("Previous Hash\t ").append(BlockTools.hashStr(this.previousHash)).append("\n");
        sb.append("Merkle Root\t ").append(BlockTools.hashStr(this.merkleHash)).append("\n");
        sb.append("Time stamp\t ").append(this.decodeTime(this.time)).append("\n");
        sb.append("Difficulty\t ").append(this.bits).append("\n");
        sb.append("Nonce\t\t ").append(this.nonce).append("\n");
        return sb.toString();
    }

    public String decodeTime(int time) {
        Instant instant = Instant.ofEpochSecond(time);
        return instant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + " (UTC)";
    }
}
