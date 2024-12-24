import java.time.Instant;

public class CuidGenerator {
    private String fingerprint;
    private int counter;

    public CuidGenerator() {
        this.fingerprint = Cuid.getProcessFingerprint();
        this.counter = -1;
    }

    public int getCounter() {
        this.counter += 1;
        if (this.counter >= Cuid.DISCRETE_VALUES) {
            this.counter = 0;
        }
        return this.counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String cuid() {
        StringBuilder identifier = new StringBuilder();
        identifier.append("c");

        long millis = Instant.now().toEpochMilli();
        identifier.append(Cuid.toBase36((int) millis));

        String count = Cuid.pad(Cuid.toBase36(getCounter()), Cuid.BLOCK_SIZE);
        identifier.append(count);

        identifier.append(this.fingerprint);

        identifier.append(Cuid.randomBlock());
        identifier.append(Cuid.randomBlock());

        return identifier.toString();
    }

    public String slug() {
        StringBuilder identifier = new StringBuilder();

        long millis = Instant.now().toEpochMilli();
        String millisString = Cuid.toBase36((int) millis);
        identifier.append(millisString.substring(millisString.length() - 2));

        String count = Cuid.pad(Cuid.toBase36(getCounter()), 1);
        identifier.append(count);

        identifier.append(this.fingerprint.charAt(0));
        identifier.append(this.fingerprint.charAt(this.fingerprint.length() - 1));

        String randomData = Cuid.randomBlock();
        identifier.append(randomData.substring(randomData.length() - 2));

        return identifier.toString();
    }
}
