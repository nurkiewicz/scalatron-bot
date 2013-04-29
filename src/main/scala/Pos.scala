case object Pos {
	val Mid = Pos(0, 0)
	def apply(s: String): Pos = {
		val Array(x, y) = s.split(":").map(_.toInt)
		Pos(x, y)
	}
}

case class Pos(x: Int, y: Int)  {
	def linear(size: Int) = y * size + x
	def euclideanDistance(other: Pos) = math.sqrt((other.x - x) * (other.x - x) + (other.y - y) * (other.y - y))
	def distance(other: Pos) = math.max((other.x - x).abs, (other.y - y).abs)
	def +(other: Pos) = Pos(x + other.x, y + other.y)
	def +(dir: Direction): Pos = this + dir.offset
	def -(other: Pos) = Pos(x - other.x, y - other.y)
	def *(value: Int) = Pos(x * value, y * value)
	def /(value: Int) = Pos(x / value, y / value)

	override def toString = x + ":" + y
	override def equals(that: Any) = that match {
		case p: Pos => p.x == x && p.y == y
		case _ => false
	}
	override def hashCode = x * 31 + y
}