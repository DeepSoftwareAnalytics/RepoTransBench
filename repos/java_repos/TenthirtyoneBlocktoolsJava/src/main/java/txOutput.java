import java.io.IOException;
import java.io.InputStream;

public class txOutput {
    public long value;
    public long scriptLen;
    public byte[] pubkey;

    public txOutput(InputStream blockchain) throws IOException {
        this.value = BlockTools.uint8(blockchain);
        this.scriptLen = BlockTools.varint(blockchain);
        this.pubkey = new byte[(int) this.scriptLen];
        blockchain.read(this.pubkey);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\tValue:\t\t ").append(this.value).append(" Satoshi\n");
        sb.append("\tScript Len:\t ").append(this.scriptLen).append("\n");
        sb.append("\tScriptPubkey:\t ").append(this.decodeScriptPubkey(this.pubkey)).append("\n");
        return sb.toString();
    }

    private String decodeScriptPubkey(byte[] data) {
        String hexstr = BlockTools.hashStr(data);
        int op_idx = Integer.parseInt(hexstr.substring(0, 2), 16);

        try {
            String op_code1 = Opcode.OPCODE_NAMES.get(op_idx);
            if ("OP_DUP".equals(op_code1)) {  // P2PKHA pay to pubkey hash mode
                String op_code2 = Opcode.OPCODE_NAMES.get(Integer.parseInt(hexstr.substring(2, 4), 16));
                int keylen = Integer.parseInt(hexstr.substring(4, 6), 16);
                String op_codeTail2nd = Opcode.OPCODE_NAMES.get(Integer.parseInt(hexstr.substring(6 + keylen * 2, 6 + keylen * 2 + 2), 16));
                String op_codeTailLast = Opcode.OPCODE_NAMES.get(Integer.parseInt(hexstr.substring(6 + keylen * 2 + 2, 6 + keylen * 2 + 4), 16));

                return String.format("Pubkey OP_CODE:\t %s %s Bytes:%d tail_op_code:%s %s\n PubkeyHash:\t %s",
                        op_code1, op_code2, keylen, op_codeTail2nd, op_codeTailLast, hexstr.substring(6, 6 + keylen * 2));
            } else if ("OP_HASH160".equals(op_code1)) {  // P2SHA pay to script hash
                int keylen = Integer.parseInt(hexstr.substring(2, 4), 16);
                String op_codeTail = Opcode.OPCODE_NAMES.get(Integer.parseInt(hexstr.substring(4 + keylen * 2, 4 + keylen * 2 + 2), 16));

                return String.format("Pubkey OP_CODE:\t %s Bytes:%d tail_op_code:%s\n Pure Pubkey:\t %s",
                        op_code1, keylen, op_codeTail, hexstr.substring(4, 4 + keylen * 2));
            } else {
                return String.format("Need to extend multi-signature parsing %x %s",
                        op_idx, op_code1);
            }
        } catch (Exception e) {
            if (op_idx > 0) {
                int keylen = op_idx;
                String op_codeTail = Opcode.OPCODE_NAMES.get(Integer.parseInt(hexstr.substring(2 + keylen * 2, 2 + keylen * 2 + 2), 16));
                return String.format("Pubkey OP_CODE:\t None Bytes:%d tail_op_code:%s\n Pure Pubkey:\t %s",
                        keylen, op_codeTail, hexstr.substring(2, 2 + keylen * 2));
            }
        }
        return hexstr;
    }
}
