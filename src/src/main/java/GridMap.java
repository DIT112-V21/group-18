

public class GridMap {
    private RGB[][] basemap;
    private int carLength; // longer side
    private int carWidth;
    private Coordinate starting_grid;
    private RGB[][] gridmap;
    private String carDirection;
    private Gridnode[][] map;
    private final RGB emptyspaceCorlor = new RGB(255, 255,255);
    private final RGB nontraversableColor = new RGB(0,0,0);
    private final RGB carColor = new RGB(255,0,0);


    public GridMap(RGB[][] map, Car car , String direction){
        this.basemap = map;
        this.carWidth = car.getWidth();
        this.carLength = car.getLength();
        this.carDirection = direction;
        this.starting_grid = car.getXy();


        rotate(carDirection);
    }

    public void generateGridmap(){
        int up = (int) Math.ceil( starting_grid.getY() / carWidth);
        int down = (int) Math.ceil((basemap.length - starting_grid.getY()) / carWidth) +1 ;
        int left = (int) Math.ceil(starting_grid.getX()/carLength);
        int right = (int) Math.ceil((basemap[0].length - starting_grid.getX()) / carLength) +1;

        int length = up + down;
        int width = left + right;
        gridmap = new RGB[length][width];
        for(int i = 0; i < gridmap.length; i++){
            for(int j = 0; j < gridmap[0].length; j++){
                gridmap[i][j] = nontraversableColor;
            }
        }
        for (int i = 0; i < basemap.length; i++){
            for(int j = 0; j < basemap[0].length; j++){
                gridmap[up - starting_grid.getY()][left - starting_grid.getX()] = basemap[i][j];
            }
        }
        starting_grid = new Coordinate(left + 1, up + 1);
    };

    //length x, width y , i vertical, j horrizontal
    public void generateMap(){
        map = new Gridnode[gridmap.length][gridmap[0].length];

        for(int i = 0; i < gridmap.length; i+= carWidth){
            for(int j = 0; j < gridmap[0].length; j+= carLength){
                int x = (int) ((i - starting_grid.getX()) / carLength);
                int y = - (int) ((j - starting_grid.getY()) / carWidth);
                Coordinate xy = new Coordinate(x, y);
                boolean traversable;
                int sum =0;
                for(int w = i; w < i + carWidth; w++ ){
                    for(int l = j; l < j + carLength; l++ ){
                        sum += gridmap[i * carWidth + w][j * carLength + l].getRed();
                    }
                }
                if(sum == emptyspaceCorlor.getRed() * carLength * carWidth){
                    traversable = true;
                }
                else{
                    traversable = false;
                    for(int w = i; w < i + carWidth; w++ ){
                        for(int l = j; l < j + carLength; l++ ){
                            gridmap[i * carWidth + w][j * carLength + l] = nontraversableColor;
                        }
                    }
                }

                map[(int) (i / carWidth)][(int) (j / carLength)] = new Gridnode(xy, traversable);
            }
        }
    }

    //car now always looks to the right side
    private void rotate(String direction){
        switch (direction){
            case "left":
                flipHorizontal();
            case "up":
                rotateRight();
            case "down":
                rotateRight();
                flipHorizontal();
        }
    }

    private void rotateRight(){
        int oldLength = basemap.length;
        int oldWidth = basemap[0].length;
        RGB[][] rotatedMap = new RGB[oldWidth][oldLength];
        for(int i = 0; i < rotatedMap.length; i++){
            for (int j = 0; j < rotatedMap[i].length; j++){
                rotatedMap[i][j] = basemap[oldLength - j][oldWidth];
            }
        }
        starting_grid = new Coordinate(oldLength - starting_grid.getY(), oldWidth);
        basemap = rotatedMap;
    }

    private void flipHorizontal(){
        for(int i = 0; i < basemap.length; i++) {
            for (int j = 0; j <= ((int) basemap[i].length / 2); j++) {
                RGB term = basemap[i][j];
                basemap[i][j] = basemap[i][basemap[i].length - j];
            }
        }
        starting_grid = new Coordinate(basemap[0].length - starting_grid.getX(), starting_grid.getY());
    }

    public RGB[][] getGridmap() {
        return gridmap;
    }

    public void colorCar(){
        for(int i = starting_grid.getY(); i < starting_grid.getY() + carLength;   i++){
            for (int j = starting_grid.getX(); j < starting_grid.getX() + carWidth; j++){
                gridmap[i][j] = carColor;
            }
        }
    }
    //car coordinate is alwasy (0, 0)
    public Gridnode[][] getMap() {
        return map;
    }
}

final class Gridnode{
    private Coordinate xy;
    private boolean traversable;


    public Gridnode(Coordinate xy, boolean traversable){
        this.xy = xy;
        this.traversable = traversable;
    }

    public Coordinate getCoordinate() {
        return xy;
    }

    public boolean isEmpty(){
        return traversable;
    }
}


final class Coordinate{ //y vertical length, x width
    private final int x, y;
    Coordinate(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX(){
        return x;
    }
}