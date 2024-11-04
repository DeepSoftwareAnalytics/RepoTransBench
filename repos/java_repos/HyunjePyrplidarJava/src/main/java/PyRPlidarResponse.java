public class PyRPlidarResponse {
    private byte syncByte1;
    private byte syncByte2;
    private int dataLength;
    private int sendMode;
    private int dataType;

    public PyRPlidarResponse(byte[] responseDescriptor) {
        if (responseDescriptor.length < 7) {
            throw new IllegalArgumentException("Response descriptor must be at least 7 bytes long.");
        }
        this.syncByte1 = responseDescriptor[0];
        this.syncByte2 = responseDescriptor[1];
        this.dataLength = ((responseDescriptor[2] & 0xFF) | ((responseDescriptor[3] << 8) & 0xFF00) | ((responseDescriptor[4] << 16) & 0xFF0000) | ((responseDescriptor[5] << 24) & 0xFF000000));
        this.sendMode = responseDescriptor[6] & 1;
        this.dataType = responseDescriptor[6] & 0xFF;
    }

    public int getDataLength() {
        return dataLength;
    }

    public int getSendMode() {
        return sendMode;
    }

    public int getDataType() {
        return dataType;
    }
}
