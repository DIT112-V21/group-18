class Car internal constructor(var length: Int, var width: Int, starting: Coordinate) {
    var starting: Coordinate
    fun getStarting(): Coordinate {
        return starting
    }

    init {
        this.starting = starting
    }
}