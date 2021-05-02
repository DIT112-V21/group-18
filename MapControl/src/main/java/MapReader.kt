//import javax.imageio.ImageIO
//import java.awt.image.BufferedImage
//import java.awt.image.DataBufferByte
//import java.io.File
//import java.io.IOException
import android.graphics.*
import android.os.Build
import android.widget.*
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream


@RequiresApi(Build.VERSION_CODES.Q)
class MapReader internal constructor(address: String?) {
    private var image: android.graphics.Bitmap
    val lENGTH: Int
    val wIDTH: Int
    lateinit var pixelMap: Array<Array<RGB>>
    private var hasAlphaChannel: Boolean
    private val pixels: ByteArray
    private val emptyspaceCorlor = RGB(255, 255, 255)
    private val nontraversableColor = RGB(79, 84, 82)



    @RequiresApi(Build.VERSION_CODES.Q)
    private fun producePixelMap() { //Make an initial 2D arrays
//        val pixelLength: Int
//        pixelLength = if (hasAlphaChannel) {
//            4
//        } else {
//            3
//        }
//        var pixel = 0
//        var row = 0
//        var col = 0
//        while (pixel + pixelLength - 1 < pixels.size) {
//            val alpha = 255
//            val blue = pixels[pixel + pixelLength - 3].toInt()
//            val green = pixels[pixel + pixelLength - 2].toInt()
//            val red = pixels[pixel + pixelLength - 1].toInt()
//            val currentPixel = RGB(red, green, blue)
//            pixelMap[row][col] = currentPixel
//            col++
//            if (col == wIDTH) {
//                col = 0
//                row++
//            }
//            pixel += pixelLength
//        }
        val pixelLength: Int
        for(i in 0 until lENGTH){
            for(j in 0 until wIDTH){
                val color: Color = image.getColor(j, i)
                val currentPixel = RGB(color.red().toInt(), color.green().toInt(), color.blue().toInt())
                pixelMap[j][i] = currentPixel
            }
        }


    }

    fun changeContrast(contrastlevel: Int) {
        for (i in pixelMap.indices) {
            for (j in 0 until pixelMap[i].size) {
                val blue = adjustContrast(pixelMap[i][j]!!.blue, contrastlevel)
                val red = adjustContrast(pixelMap[i][j]!!.red, contrastlevel)
                val green = adjustContrast(pixelMap[i][j]!!.green, contrastlevel)
                pixelMap[i][j] = RGB(red, green, blue)
            }
        }
    }

    fun adjustVibrance() {
        for (i in pixelMap.indices) {
            for (j in 0 until pixelMap[i].size) {
                val currentpixel = pixelMap[i][j]
                pixelMap[i][j] = RGB((0.3 * currentpixel!!.red).toInt(), (0.59 * currentpixel.green).toInt(), (0.11 * currentpixel.blue).toInt())
            }
        }
    }

    fun convertToTraverse() {
        for (i in pixelMap.indices) {
            for (j in 0 until pixelMap[i].size) {
                val currentpixel = pixelMap[i][j]
                var red = currentpixel!!.red
                var green = currentpixel.green
                val blue = currentpixel.blue
                if (red < 0) {
                    red += 255
                }
                if (green < 0) {
                    green += 255
                }
                if (red > 100 || green > 100) {
                    pixelMap[i][j] = nontraversableColor
                } else {
                    pixelMap[i][j] = emptyspaceCorlor
                    val rgb = pixelMap[i][j]
                }
            }
        }
        System.out.println("wtf")
    }





    companion object {
        private fun adjustContrast(color: Int, CONSTRAST_LEVEL: Int): Int {
            val CONTRAST_FACTOR = (259 * (CONSTRAST_LEVEL + 255) / (255 * (259 - CONSTRAST_LEVEL))).toFloat()
            return CONTRAST_FACTOR.toInt() * (color - 128) + 128
        }
    }

    init {
        image = BitmapFactory.decodeFile(address)


        lENGTH = image.height
        wIDTH = image.width

        hasAlphaChannel = image.hasAlpha()
        val stream = ByteArrayOutputStream()


        image.compress(Bitmap.CompressFormat.PNG, 100 , stream)
        pixels = stream.toByteArray()
        producePixelMap()
    }
}

class RGB(val red: Int, val green: Int, val blue: Int) {
    private val Alpha = 255
    var rgb: Int
    fun compare(a: RGB): Int {
        return rgb - a.rgb
    }

    companion object {
        fun correctedRGB(a: Int): Int {
            return if (a < 0) 255 + a else a
        }
    }

    init {
        rgb = red
        rgb = (rgb shl 8) + green
        rgb = (rgb shl 8) + blue
    }
}