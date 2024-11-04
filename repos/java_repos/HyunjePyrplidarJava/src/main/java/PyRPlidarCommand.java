public class PyRPlidarCommand {
    public static final byte RPLIDAR_CMD_STOP = 0x25;
    public static final byte RPLIDAR_CMD_RESET = 0x40;
    public static final byte RPLIDAR_CMD_GET_INFO = 0x50;

    private byte[] cmd;

    public PyRPlidarCommand(byte command) {
        this.cmd = new byte[]{(byte) 0xA5, command};
    }

    public byte[] getRawBytes() {
        return this.cmd;
    }
}
