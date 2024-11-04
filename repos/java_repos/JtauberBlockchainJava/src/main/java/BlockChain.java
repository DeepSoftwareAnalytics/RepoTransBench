import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BlockChain {
    private byte[] data;
    private int index;
    private int blockCount;

    public BlockChain(byte[] data) {
        this.data = data;
        this.index = 0;
        this.blockCount = 0;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(int blockCount) {
        this.blockCount = blockCount;
    }

    public Iterable<Block> blocks() {
        List<Block> blocks = new ArrayList<>();
        while (index < data.length) {
            blocks.add(parseBlock());
            blockCount++;
        }
        return blocks;
    }

    public String hashSince(int mark) {
        return toHex(doubleHash(data, mark, index));
    }

    public int getUint8() {
        return data[index++] & 0xFF;
    }

    public int getUint16() {
        return getUint8() | (getUint8() << 8);
    }

    public long getUint32() {
        return getUint16() | ((long) getUint16() << 16);
    }

    public long getUint64() {
        return getUint32() | (getUint32() << 32);
    }

    public byte[] getBytestring(int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, index, result, 0, length);
        index += length;
        return result;
    }

    public Date getTimestamp() {
        return new Date(getUint32() * 1000L);
    }

    public String getHash() {
        return toHex(getBytestring(32));
    }

    public long getVarlenInt() {
        int code = getUint8();
        if (code < 0xFD) {
            return code;
        } else if (code == 0xFD) {
            return getUint16();
        } else if (code == 0xFE) {
            return getUint32();
        } else {
            return getUint64();
        }
    }

    public List<String> getScript() {
        long scriptLength = getVarlenInt();
        byte[] script = getBytestring((int) scriptLength);
        List<String> tokens = new ArrayList<>();

        int scriptIndex = 0;
        while (scriptIndex < scriptLength) {
            int opCode = script[scriptIndex++] & 0xFF;
            if (opCode <= 75) {
                tokens.add(new String(script, scriptIndex, opCode));
                scriptIndex += opCode;
            } else if (opCode == 97) {
                tokens.add("OP_NOP");
            } else if (opCode == 118) {
                tokens.add("OP_DUP");
            } else if (opCode == 136) {
                tokens.add("OP_EQUALVERIFY");
            } else if (opCode == 169) {
                tokens.add("OP_HASH160");
            } else if (opCode == 172) {
                tokens.add("OP_CHECKSIG");
            } else {
                throw new IllegalArgumentException("Unknown opcode: " + opCode);
            }
        }
        return tokens;
    }

    public Block parseBlock() {
        if (getUint32() != 0xD9B4BEF9) {
            throw new IllegalArgumentException("Invalid magic network id");
        }

        long blockLength = getUint32();
        int mark = index;

        Block block = new Block();
        block.setSize(blockLength);
        block.setVer(getUint32());
        block.setPrevBlock(getHash());
        block.setMrklRoot(getHash());
        block.setTimestamp(getTimestamp());
        block.setBits(getUint32());
        block.setNonce(getUint32());
        block.setHash(hashSince(mark));

        long transactionCount = getVarlenInt();
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < transactionCount; i++) {
            transactions.add(parseTransaction());
        }
        block.setTransactions(transactions);

        return block;
    }

    public Transaction parseTransaction() {
        int mark = index;

        Transaction transaction = new Transaction();
        transaction.setVer(getUint32());
        transaction.setInputs(parseInputs());
        transaction.setOutputs(parseOutputs());
        transaction.setLockTime(getUint32());
        transaction.setHash(hashSince(mark));

        return transaction;
    }

    public List<Input> parseInputs() {
        long count = getVarlenInt();
        List<Input> inputs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Input input = new Input();
            input.setTransactionHash(getHash());
            input.setTransactionIndex(getUint32());
            input.setScript(getScript());
            input.setSequenceNumber(getUint32());
            inputs.add(input);
        }
        return inputs;
    }

    public List<Output> parseOutputs() {
        long count = getVarlenInt();
        List<Output> outputs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Output output = new Output();
            output.setValue(getUint64());
            output.setScript(getScript());
            outputs.add(output);
        }
        return outputs;
    }

    public static byte[] doubleHash(byte[] input) {
        return doubleHash(input, 0, input.length);
    }

    public static byte[] doubleHash(byte[] input, int offset, int length) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(input, offset, length);
            byte[] firstHash = sha256.digest();
            return sha256.digest(firstHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String base58(byte[] input) {
        return encodeBase58(input);
    }

    private static final String BASE58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    public static String encodeBase58(byte[] input) {
        if (input.length == 0) {
            return "";
        }
        // Count leading zeros.
        int zeros = 0;
        while (zeros < input.length && input[zeros] == 0) {
            ++zeros;
        }
        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        StringBuilder encoded = new StringBuilder();
        for (int i = zeros; i < input.length; i++) {
            int carry = input[i] & 0xFF;
            for (int j = zeros; j < encoded.length(); j++) {
                carry += (encoded.charAt(j) - '1') * 256;
                encoded.setCharAt(j, BASE58_ALPHABET.charAt(carry % 58));
                carry /= 58;
            }
            while (carry > 0) {
                encoded.append(BASE58_ALPHABET.charAt(carry % 58));
                carry /= 58;
            }
        }
        // Add leading zeros
        for (int i = 0; i < zeros; i++) {
            encoded.append(BASE58_ALPHABET.charAt(0));
        }
        return encoded.reverse().toString();
    }
}

