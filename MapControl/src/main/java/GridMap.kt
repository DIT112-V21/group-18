class GridMap(map: Array<Array<RGB?>>, car: Car, direction: String) {
    private var basemap: Array<Array<RGB?>>
    private val carLength // longer side
            : Double
    private val carWidth: Double
    private var starting_pixel: Coordinate
    private var borderAdjustedMap: Array<Array<RGB?>>
    private val carDirection: String

    //car coordinate is always (0, 0)
    val map: Array<Array<Gridnode>>
    private val emptyspaceCorlor: RGB = RGB(255, 255, 255)
    private val nontraversableColor: RGB = RGB(79, 84, 82)
    private val carColor: RGB = RGB(255, 0, 0)
    private val headColor: RGB = RGB(179, 198, 255)
    private val cull_ratio = 0.85
    private val getStarting_pixel1: Coordinate? = null
    private var upB = 0.0
    private var downB = 0.0
    private var leftB = 0.0
    private var rightB = 0.0

    //get new border cover overflow edge based on car's measure
    fun adjustMapBorder() {
        upB = carLength - starting_pixel.verr % carLength
        downB = carLength - (basemap.size - starting_pixel.verr) % carLength
        leftB = carWidth - starting_pixel.horr % carWidth
        rightB = carWidth - (basemap[0].length - starting_pixel.horr) % carWidth
        val length = basemap.size + upB + downB
        val width: Double = basemap[0].length + leftB + rightB
        borderAdjustedMap = Array<Array<RGB?>>(length.toInt()) { arrayOfNulls<RGB>(width.toInt()) }
        run {
            var i = 0
            while (i < length) {
                var j = 0
                while (j < width) {
                    borderAdjustedMap[i][j] = nontraversableColor
                    j++
                }
                i++
            }
        }
        for (i in basemap.indices) {
            for (j in 0 until basemap[0].length) {
                borderAdjustedMap[(i + upB)][(j + leftB)] = basemap[i][j] // wtf did I do here?
            }
        }
        starting_pixel = Coordinate(starting_pixel.verr + upB.toInt(), starting_pixel.horr + leftB.toInt())
        var i = 0
        while (i < upB) {
            var j = leftB.toInt()
            while (j < width - rightB - 1) {
                if (borderAdjustedMap[upB.toInt()][j].compare(emptyspaceCorlor) === 0) {
                    borderAdjustedMap[i][j] = emptyspaceCorlor
                } else {
                    borderAdjustedMap[i][j] = nontraversableColor
                }
                j++
            }
            i++
        }



        //        for(int i = 0; i < downB; i++){
//            for(int j = (int) leftB; j < width - rightB - 1; j++){
//                if(borderAdjustedMap[(int)(length - 1 - downB)][j].compare(emptyspaceCorlor) == 0 ){
//                    borderAdjustedMap[i][j] = emptyspaceCorlor;
//                }
//                else{
//                    borderAdjustedMap[i][j] = nontraversableColor;
//                }
//            }
//        }
//        for(int i = 0; i < length; i++){
//            for(int j = 0; j < leftB; j++){
//                if(borderAdjustedMap[i][(int) leftB + 1].compare(emptyspaceCorlor) == 0 ){
//                    borderAdjustedMap[i][j] = emptyspaceCorlor;
//                }
//                else{
//                    borderAdjustedMap[i][j] = nontraversableColor;
//                }
//            }
//        }
//        for(int i = 0; i < length; i++){
//            for(int j = 0; j < rightB; j++){
//                if(borderAdjustedMap[i][ (int)(width -  leftB - 1)].compare(emptyspaceCorlor) == 0 ){
//                    borderAdjustedMap[i][j] = emptyspaceCorlor;
//                }
//                else{
//                    borderAdjustedMap[i][j] = nontraversableColor;
//                }
//            }
//        }


        //these coordinate are pixel coordinate
    }

    //generate grid map based on the adjusted map
    fun generateGridMap() {
        val gridMap = Array(borderAdjustedMap.size) { arrayOfNulls<Gridnode>(borderAdjustedMap[0].length) }

        //loop from  left to right, top to bottom, vertical outer loop
        var i = 0
        while (i < borderAdjustedMap.size) {
            var j = 0
            while (j < borderAdjustedMap[0].length) {
                val verr = ((i - starting_pixel.verr) / carLength).toInt()
                val horr = (-((j - starting_pixel.horr) / carWidth)).toInt()
                val carcoor = Coordinate(verr, horr)
                var traversable: Boolean
                var sum = 0

                //loop the grid node
                var l = 0
                while (l < carLength) {
                    var w = 0
                    while (w < carWidth) {
                        val f = j + w
                        sum += borderAdjustedMap[i + l][j + w].getRed()
                        w++
                    }
                    l++
                }
                if (i == starting_pixel.verr && j == starting_pixel.horr) {
                    traversable = true
                    var l = 0
                    while (l < carLength) {
                        var w = 0
                        while (w < carWidth) {
                            if (l <= 0.2 * carLength) {
                                borderAdjustedMap[i + l][j + w] = headColor
                            } else {
                                borderAdjustedMap[i + l][j + w] = carColor
                            }
                            w++
                        }
                        l++
                    }
                } else if (sum >= cull_ratio * (emptyspaceCorlor.getRed() * carLength * carWidth) as Double) {
                    traversable = true
                    var l = 0
                    while (l < carLength) {
                        var w = 0
                        while (w < carWidth) {
                            borderAdjustedMap[i + l][j + w] = emptyspaceCorlor
                            w++
                        }
                        l++
                    }
                } else {
                    traversable = false
                    var l = 0
                    while (l < carLength) {
                        var w = 0
                        while (w < carWidth) {
                            borderAdjustedMap[i + l][j + w] = nontraversableColor
                            w++
                        }
                        l++
                    }
                }
                gridMap[(i / carWidth).toInt()][(j / carLength).toInt()] = Gridnode(carcoor, traversable)
                j += carWidth.toInt()
            }
            i += carLength.toInt()
        }
    }

    //car now always looks to the top of the map
    private fun rotate(direction: String) {
        when (direction) {
            "down" -> flipVertical()
            "left" -> rotateRight()
            "right" -> {
                rotateRight()
                flipVertical()
            }
            else -> {
            }
        }
    }

    private fun rotateRight() {
        val oldLength = basemap.size
        val oldWidth: Int = basemap[0].length
        val rotatedMap: Array<Array<RGB?>> = Array<Array<RGB?>>(oldWidth) { arrayOfNulls<RGB>(oldLength) }
        for (i in rotatedMap.indices) {
            for (j in 0 until rotatedMap[i].length) {
                rotatedMap[i][j] = basemap[oldLength - j - 1][i]
            }
        }
        starting_pixel = Coordinate(starting_pixel.horr, oldLength - starting_pixel.verr - 1)
        starting_pixel = Coordinate(starting_pixel.verr, (starting_pixel.horr - carWidth + 1).toInt())
        basemap = rotatedMap
    }

    private fun flipVertical() {
        for (i in 0 until (basemap.size / 2)) {
            for (j in 0 until basemap[i].length) {
                val term: RGB? = basemap[i][j]
                basemap[i][j] = basemap[basemap.size - i - 1][j]
                basemap[basemap.size - i - 1][j] = term
            }
        }
        val rotated = Coordinate(basemap.size - starting_pixel.verr - 1, starting_pixel.horr)
        starting_pixel = Coordinate((rotated.verr - carLength + 1).toInt(), rotated.horr)
        starting_pixel = Coordinate((starting_pixel.verr - carLength + 1).toInt(), starting_pixel.horr)
    }

    fun getBasemap(): Array<Array<RGB?>> {
        return basemap
    }

    fun getBorderAdjustedMap(): Array<Array<RGB?>> {
        return borderAdjustedMap
    }

    init {
        basemap = map
        carWidth = car.getWidth()
        carLength = car.getLength()
        carDirection = direction
        starting_pixel = car.getStarting()
        for (i in 0..19) {
            for (j in 0..19) {
                basemap[i][j] = headColor
            }
        }
        rotate(carDirection)
    }
}

class Gridnode(val coordinate: Coordinate, val isEmpty: Boolean)
internal class Coordinate(val verr: Int, //y vertical length, x width
                          val horr: Int)