import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Sight {

    public static void parse(InputStream blockchain, int blkNo) throws IOException {
        System.out.println("Parsing Block Chain block head, transaction etc.");
        boolean continueParsing = true;
        int counter = 0;

        blockchain.skip(blockchain.available() - 80);  // Minus last Block header size for partial file
        blockchain.reset();

        while (continueParsing) {
            Block block = new Block(blockchain);
            continueParsing = block.continueParsing();

            if (continueParsing) {
                block.toString();
            }

            counter++;
            System.out.println("####################" + "Block counter No. " + counter + "####################");

            if (counter >= blkNo && blkNo != 0xFF) {
                continueParsing = false;
            }
        }

        System.out.println();
        System.out.println("Reached End of Field");
        System.out.println("Parsed " + counter + " blocks");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: Sight filename");
        } else {
            int blkNo = 0xFF;
            if (args.length == 2) {
                blkNo = Integer.parseInt(args[1]);
                System.out.println("Parsing " + blkNo + " blocks");
            }

            try (InputStream blockchain = new FileInputStream(args[0])) {
                parse(blockchain, blkNo);
            }
        }
    }
}
