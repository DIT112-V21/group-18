import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;


public class MapReader {
    private BufferedImage image;
    private final int LENGTH;
    private final int WIDTH;
    private RGB[][] pixelmap;
    private final boolean hasAlphaChannel;
    private final byte[] pixels;
    private final RGB emptyspaceCorlor = new RGB(255, 255,255);
    private final RGB nontraversableColor = new RGB(79,84,82);

    MapReader(String address) throws IOException {
        image = ImageIO.read(new File(address));

        LENGTH = image.getHeight();
        WIDTH = image.getWidth();
        pixelmap = new RGB[LENGTH][WIDTH];
        hasAlphaChannel = (image.getAlphaRaster() != null);
        pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        producePixelMap();
    }

    private void producePixelMap() { //Make an initial 2D arrays
        int pixelLength;
        if(hasAlphaChannel) {
            pixelLength = 4;
        }
        else {
            pixelLength = 3;
        }
        for(int pixel = 0, row = 0, col = 0; pixel + pixelLength - 1 < pixels.length; pixel += pixelLength) {
            int alpha = 255;
            int blue = (int) pixels[pixel + pixelLength - 3];
            int green = (int) pixels[pixel +pixelLength - 2];
            int red = (int) pixels[pixel + pixelLength - 1];

            RGB currentPixel = new RGB(red, green, blue);
            pixelmap[row][col] = currentPixel;
            col++;
            if(col == WIDTH) {
                col = 0;
                row++;
            }

        }
    }

    public void changeContrast( int contrastlevel) {
        for(int i = 0; i < pixelmap.length; i++) {
            for(int j = 0; j < pixelmap[i].length; j++) {
                int blue = adjustContrast(pixelmap[i][j].getBlue(), contrastlevel);
                int red = adjustContrast(pixelmap[i][j].getRed(), contrastlevel);
                int green = adjustContrast(pixelmap[i][j].getGreen(), contrastlevel);

                pixelmap[i][j] = new RGB(red, green, blue);

            }
        }
    }

    public void adjustVibrance() {
        for(int i = 0; i < pixelmap.length; i++) {
            for(int j = 0; j < pixelmap[i].length; j++) {
                RGB currentpixel= pixelmap[i][j];

                pixelmap[i][j] = new RGB((int) (0.3 * currentpixel.getRed()), (int) (0.59 * currentpixel.getGreen()), (int ) (0.11 * currentpixel.getBlue())) ;
            }

        }
    }

    public void convertToTraverse(){
        for(int i = 0; i < pixelmap.length; i++) {
            for (int j = 0; j < pixelmap[i].length; j++) {
                RGB currentpixel= pixelmap[i][j];
                int red = currentpixel.getRed();
                int green = currentpixel.getGreen();
                int blue = currentpixel.getBlue();

                if(red<0){
                    red += 255;
                }
                if (green <0 ){
                    green += 255;
                }

                if( red > 100 || green > 100 ){
                    pixelmap[i][j] = nontraversableColor;
                }
                else {
                    RGB rgb = pixelmap[i][j] = emptyspaceCorlor;
                }
            }
        }
    }

    private static int adjustContrast(int color, int CONSTRAST_LEVEL) {
        float CONTRAST_FACTOR = (259 * (CONSTRAST_LEVEL + 255)) / (255 * (259 - CONSTRAST_LEVEL));
        return (int) CONTRAST_FACTOR * (color -128) +128;
    }
    public RGB[][] getPixelMap(){
        return pixelmap;
    }
    public int getLENGTH(){
        return  LENGTH;
    }
    public int getWIDTH(){
        return WIDTH;
    }


}

final class RGB{
    private int Red;
    private int Green;
    private int Blue;
    private int Alpha;
    private int rgb;

    RGB(int red, int green, int blue){
        this.Red = red;
        this.Green = green;
        this.Blue = blue;
        this.Alpha = 255;
        rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
    }


    public int getRed() {
        return Red;
    };

    public int getBlue() {
        return Blue;
    }

    public int getGreen() {
        return Green;
    }

    public int getRgb(){
        return rgb;
    }

}