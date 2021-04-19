import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;


public class Main {

    private static int BASE_CONTRAST_LEVEL = 128;
    private static RGB body_color = new RGB(253, 235, 231);
    private static RGB head_color = new RGB(38,38,42);
    private static final String CAR_DIRECTION = "left";

    public static void main(String[] args) throws Exception{

        MapReader map = new MapReader("map.png");
        RGB[][] pixelMap = map.getPixelMap();
//        Car car = detectCar(pixelMap);
        map.changeContrast( BASE_CONTRAST_LEVEL);
        map.adjustVibrance();

        map.convertToTraverse();
        pixelMap = map.getPixelMap();


//        GridMap refinedMap = new GridMap(pixelMap, car, CAR_DIRECTION);
//        refinedMap.generateGridmap();
//        refinedMap.colorCar();
//        pixelMap = refinedMap.getGridmap();
//        refinedMap.generateMap();




        BufferedImage img = picture( map.getWIDTH(), map.getLENGTH(), pixelMap );
        savePNG( img, "./src/src/main/resources/" );
    }

    private static Car detectCar(RGB[][] pixelMap){
        Coordinate xy = new Coordinate(-1, -1);
        int carWidth=0;
        int carLength=0;
        for(int i= 0; i< pixelMap.length; i++){
            for(int j=0; j < pixelMap[0].length; j++){
                if(pixelMap[i][j] == head_color){
                    xy = new Coordinate(j, i);
                    j+=pixelMap[0].length;
                    i+=pixelMap.length;
                }
            }
        }
        if(xy.getX() == -1){
            return null;
        }
        for(int i = 0; i< pixelMap.length; i++){
            for(int j = 0; j < pixelMap[0].length; j++) {
                if(pixelMap[i][j] == head_color){
                    if(CAR_DIRECTION=="left" || CAR_DIRECTION=="right"){
                        carLength = Math.max(carLength, j - xy.getX());
                        carWidth = (int)(85* carLength/100);
                    }
                    else{
                        carLength= Math.max(carWidth, i- xy.getY());
                        carWidth = (int)(85* carLength/100);
                    }
                }
            }
        }
        return new Car(xy, carWidth,carLength);
    }

    private static BufferedImage picture( int sizeX, int sizeY , RGB[][] pixelMap){
        final BufferedImage res = new BufferedImage( sizeX, sizeY, BufferedImage.TYPE_INT_RGB );
        for (int x = 0; x < sizeX; x++){
            for (int y = 0; y < sizeY; y++){
                res.setRGB(x, y, pixelMap[y][x].getRgb());
            }
        }
        return res;

    }

    private static void savePNG( final BufferedImage bi, final String path ){
        try {
            RenderedImage rendImage = bi;
            ImageIO.write(rendImage, "bmp", new File(path + "Processed Map.PNG"));
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }


}
