public class Piece {
    // Define the attributes and methods for the Piece class

    public static final int FACE = 1;
    public static final int EDGE = 2;
    public static final int CORNER = 3;

    private String[] colors;
    private int type;
    private int position;

    public Piece(String[] colors, int type, int position) {
        this.colors = colors;
        this.type = type;
        this.position = position;
    }

    public String[] getColors() {
        return colors;
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }
}
