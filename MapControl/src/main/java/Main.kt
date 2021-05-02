import android.os.Build
import androidx.annotation.RequiresApi

class Main {
    private val nontraversableColor: RGB = RGB(79, 84, 82)

    companion object {
        private const val BASE_CONTRAST_LEVEL = 128
        private const val CAR_DIRECTION = "up"
        private const val bodyToTotalRatio = 0.35

        @RequiresApi(Build.VERSION_CODES.Q)
        @Throws(Exception::class)
        fun main(args: Array<String?>?) {
            val map = MapReader("map.png")
            var pixelMap: Array<Array<RGB>> = map.pixelMap
            val car: Car = detectCar(pixelMap)
            map.changeContrast(BASE_CONTRAST_LEVEL)
            map.adjustVibrance()
            map.convertToTraverse()
            pixelMap = map.pixelMap
            val refinedMap = GridMap(pixelMap, car, CAR_DIRECTION)
            pixelMap = refinedMap.getBasemap()
            refinedMap.adjustMapBorder()
            refinedMap.generateGridMap()
            pixelMap = refinedMap.getBorderAdjustedMap()
//            val img: BufferedImage = picture(pixelMap.size, pixelMap[0].length, pixelMap)
//            savePNG(img, "./src/main/resources/")
        }

        private fun detectCar(pixelMap: Array<Array<RGB>>): Car {
            var count = 0
            var detected = false
            var starting = Coordinate(-1, -1)
            val firstBodyToTotalPixel: Double = (Math.sqrt(1 / bodyToTotalRatio) - 1) / 2
            for (i in pixelMap.indices) {
                for (j in pixelMap[0].indices) {
                    val red: Int = RGB.correctedRGB(pixelMap[i][j].red)
                    val green: Int = RGB.correctedRGB(pixelMap[i][j].green)
                    val blue: Int = RGB.correctedRGB(pixelMap[i][j].blue)
                    if (red + green + blue > 620) {
                        if (!detected) {
                            starting = Coordinate(i, j)
                            detected = true
                        }
                        count++
                    }
                }
            }
            val totalsize = count / bodyToTotalRatio
            val width = Math.ceil(Math.sqrt(totalsize / 1.25)) as Int
            val length = Math.ceil(width * 1.25) as Int
            starting = Coordinate((starting.verr - length * firstBodyToTotalPixel) as Int, (starting.verr - width * firstBodyToTotalPixel) as Int)
            return Car(length, width, starting)
        }

//        private fun picture(sizeX: Int, sizeY: Int, pixelMap: Array<Array<RGB>>): BufferedImage {
//            val res = BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB)
//            for (x in 0 until sizeX) {
//                for (y in 0 until sizeY) {
//                    res.setRGB(x, y, pixelMap[x][sizeY - y - 1].getRgb())
//                }
//            }
//            return res
//        }
//
//        private fun savePNG(bi: BufferedImage, path: String) {
//            try {
//                val rendImage: RenderedImage = bi
//                ImageIO.write(rendImage, "bmp", File(path + "Processed Map.PNG"))
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
    }
}