public class PyRPlidarScanUltraCapsule {
    public static int[] varbitscaleDecode(int input) {
        int[] result = new int[2];
        result[1] = input & 0x03; // scale level
        switch (result[1]) {
            case 0:
                result[0] = input >> 2;
                break;
            case 1:
                result[0] = (input >> 2) << 1;
                break;
            case 2:
                result[0] = (input >> 2) << 2;
                break;
            case 3:
                result[0] = (input >> 2) << 3;
                break;
        }
        return result;
    }
}
