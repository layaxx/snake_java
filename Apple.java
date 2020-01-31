public class Apple {

    private int xPosition;
    private int yPosition;

    public Apple(int max) {
        xPosition = (int) (Math.random() * max);
        yPosition = (int) (Math.random() * max);
    }

    public int getXPosition() {
        return xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }
}