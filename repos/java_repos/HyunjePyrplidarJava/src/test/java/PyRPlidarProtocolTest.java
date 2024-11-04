import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PyRPlidarProtocolTest {

    @Test
    public void testStopReqPacket() {
        PyRPlidarCommand cmd = new PyRPlidarCommand(PyRPlidarCommand.RPLIDAR_CMD_STOP);
        assertEquals(cmd.getRawBytes(), new byte[]{(byte) 0xA5, 0x25});
    }

    @Test
    public void testResetReqPacket() {
        PyRPlidarCommand cmd = new PyRPlidarCommand(PyRPlidarCommand.RPLIDAR_CMD_RESET);
        assertEquals(cmd.getRawBytes(), new byte[]{(byte) 0xA5, 0x40});
    }

    @Test
    public void testGetInfoReqPacket() {
        PyRPlidarCommand cmd = new PyRPlidarCommand(PyRPlidarCommand.RPLIDAR_CMD_GET_INFO);
        assertEquals(cmd.getRawBytes(), new byte[]{(byte) 0xA5, 0x50});
    }

    @Test
    public void testParseDescriptor01() {
        PyRPlidarResponse descriptor = new PyRPlidarResponse(new byte[]{(byte) 0xA5, 0x5A, 0x04, 0x00, 0x00, 0x00, 0x15});
        assertEquals(descriptor.getDataLength(), 0x04);
        assertEquals(descriptor.getSendMode(), 0);
        assertEquals(descriptor.getDataType(), 0x15);
    }

    @Test
    public void testParseDescriptor02() {
        PyRPlidarResponse descriptor = new PyRPlidarResponse(new byte[]{(byte) 0xA5, 0x5A, (byte) 0x84, 0x00, 0x00, 0x40, (byte) 0x84});
        assertEquals(descriptor.getDataLength(), 0x84);
        assertEquals(descriptor.getSendMode(), 1);
        assertEquals(descriptor.getDataType(), 0x84);
    }

    @Test
    public void testVarbitscaleDecode() {
        int[] distMajorInput = {0x1E0, 0x20B, 0x219, 0x504, 0x507, 0x51E};
        int[] distMajorOutput = {0x1E0, 0x216, 0x232, 0x810, 0x81C, 0x878};
        int[] scalelvlOutput = {0, 1, 1, 2, 2, 2};

        for (int i = 0; i < distMajorInput.length; i++) {
            int[] result = PyRPlidarScanUltraCapsule.varbitscaleDecode(distMajorInput[i]);
            assertEquals(result[0], distMajorOutput[i]);
            assertEquals(result[1], scalelvlOutput[i]);
        }
    }

    @Test
    public void testCapsuleParsing() {
        int[][][] nodesResult = {
        };

        byte[][] data = {
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE2, (byte) 0x82, (byte) 0x97, (byte) 0xC},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE2, (byte) 0xEE, (byte) 0x97, (byte) 0x0},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE3, (byte) 0x2D, (byte) 0x96, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE3, (byte) 0xB0, (byte) 0x95, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE3, (byte) 0xF1, (byte) 0x96, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE4, (byte) 0x5D, (byte) 0x96, (byte) 0xC},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE4, (byte) 0xB3, (byte) 0x96, (byte) 0x0},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE4, (byte) 0xDB, (byte) 0x96, (byte) 0x0},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE5, (byte) 0x49, (byte) 0x95, (byte) 0xC},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE5, (byte) 0x9F, (byte) 0x95, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE6, (byte) 0x0B, (byte) 0x94, (byte) 0xC},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE6, (byte) 0x4C, (byte) 0x95, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE6, (byte) 0xA2, (byte) 0x96, (byte) 0x0},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE7, (byte) 0x0E, (byte) 0x95, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE7, (byte) 0x63, (byte) 0x95, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE7, (byte) 0xA4, (byte) 0x94, (byte) 0xC},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE7, (byte) 0xFA, (byte) 0x93, (byte) 0xC},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE8, (byte) 0x4F, (byte) 0x93, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE8, (byte) 0xA7, (byte) 0x93, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE9, (byte) 0x13, (byte) 0x93, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE9, (byte) 0x69, (byte) 0x93, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE9, (byte) 0xBE, (byte) 0x93, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xE9, (byte) 0xE9, (byte) 0x93, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xEA, (byte) 0x55, (byte) 0x93, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xEA, (byte) 0xC1, (byte) 0x93, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xEB, (byte) 0x02, (byte) 0x93, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xEB, (byte) 0x58, (byte) 0x93, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xEB, (byte) 0xC4, (byte) 0x94, (byte) 0x0},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xEC, (byte) 0x19, (byte) 0x94, (byte) 0xC},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xEC, (byte) 0x44, (byte) 0x94, (byte) 0x8},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xEC, (byte) 0xB0, (byte) 0x95, (byte) 0x4},
            {(byte) 0x2, (byte) 0xBC, (byte) 0xED, (byte) 0x05, (byte) 0x96, (byte) 0x8}
        };

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < nodesResult[i].length; j++) {
                int[] result = PyRPlidarScanUltraCapsule.varbitscaleDecode(data[i][j]);
                assertEquals(result[0], nodesResult[i][j][2]);
                assertEquals(result[1], nodesResult[i][j][3]);
            }
        }
    }
}