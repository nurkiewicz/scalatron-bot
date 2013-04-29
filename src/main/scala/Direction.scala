case object Direction {
	private val dict = Vector(Pos(1, 0), Pos(1, 1), Pos(0, 1), Pos(-1, 1), Pos(-1, 0), Pos(-1, -1), Pos(0, -1), Pos(1, -1))

	private def angle(p: Pos) = (((math.atan2(p.y, p.x) * 180 / math.Pi + 360) % 360) / 45).toInt

	def apply(pos: Pos): Direction = if(pos != Pos.Mid)
		Direction.apply(angle(pos) % 8)
	else {
		Direction.apply((math.random * 8).toInt)
	}

	val East = new Direction(0)
	val SouthEast = new Direction(1)
	val South = new Direction(2)
	val SouthWest = new Direction(3)
	val West = new Direction(4)
	val NorthWest = new Direction(5)
	val North = new Direction(6)
	val NorthEast = new Direction(7)

	val All = Seq(East, SouthEast, South, SouthWest, West, NorthWest, North, NorthEast)
}

case class Direction(dirIdx: Int) {
	val offset = Direction.dict(dirIdx)
	override def toString = offset.x + ":" + offset.y
	def rotateLittleBitLeft = Direction((dirIdx + 7) % 8)
	def rotateLittleBitRight = Direction((dirIdx + 1) % 8)
	def turnBack = Direction((dirIdx + 4) % 8)
}