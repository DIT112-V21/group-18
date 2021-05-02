class Car internal constructor(var length: Int, var width: Int, starting: Coordinate) {
    var starting: Coordinate


    init {
        this.starting = starting
    }
}