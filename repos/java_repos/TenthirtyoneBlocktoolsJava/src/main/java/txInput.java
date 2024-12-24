import java.io.IOException;
import java.io.InputStream;
import static java.nio.charset.StandardCharsets.UTF_8;

public class txInput {
    public byte[] prevhash;
    public int txOutId;
    public long scriptLen;
    public byte[] scriptSig;
    public int seqNo;

    public txInput(InputStream blockchain) throws IOException {
        this.prevhash = BlockTools.hash32(blockchain);
        this.txOutId = BlockTools.uint4(blockchain);
        this.scriptLen = BlockTools.varint(blockchain);
        this.scriptSig = new byte[(int) this.scriptLen];
        blockchain.read(this.scriptSig);
        this.seqNo = BlockTools.uint4(blockchain);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.txOutId == 0xffffffff) {
            sb.append("\tCoinbase Text:\t ").append(new String(this.prevhash, UTF_8)).append("\n");
        } else {
            sb.append("\tPrev. Tx Hash:\t ").append(BlockTools.hashStr(this.prevhash)).append("\n");
        }

        sb.append("\tTx Out Index:\t ").append(this.decodeOutIdx(this.txOutId)).append("\n");
        sb.append("\tScript Length:\t ").append(this.scriptLen).append("\n");
        this.decodeScriptSig(this.scriptSig, sb);
        sb.append("\tSequence:\t ").append(String.format("%8x", this.seqNo)).append("\n");
        return sb.toString();
    }

    private void decodeScriptSig(byte[] data, StringBuilder sb) {
        String hexstr = BlockTools.hashStr(data);
        if (this.txOutId == 0xffffffff) {
            sb.append(hexstr);
        } else {
            int scriptLen = Integer.parseInt(hexstr.substring(0, 2), 16) * 2;
            String script = hexstr.substring(2, 2 + scriptLen);
            sb.append("\tScript:\t\t ").append(script).append("\n");

            if (Opcode.SIGHASH_ALL != Integer.parseInt(hexstr.substring(scriptLen, scriptLen + 2), 16)) {
                sb.append("\t Script op_code is not SIGHASH_ALL").append("\n");
            } else {
                String pubkey = hexstr.substring(2 + scriptLen + 2, 2 + scriptLen + 2 + 66);
                sb.append("\tInPubkey:\t ").append(pubkey).append("\n");
            }
        }
    }

    private String decodeOutIdx(int idx) {
        if (idx == 0xffffffff) {
            return String.format("%8x Coinbase with special index", idx);
        } else {
            return String.format("%8x", idx);
        }
    }
}
