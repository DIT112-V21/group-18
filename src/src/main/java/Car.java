public class Car {
    private final Coordinate xy;
    private final int width;
    private final int length;

    Car(Coordinate xy, int width, int length){
        this.xy= xy;
        this.width = width;
        this.length = length;
    }

    public Coordinate getXy() {
        return xy;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }
}
