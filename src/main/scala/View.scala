
class View(view: String) {
	val size = math.sqrt(view.size).toInt
	private val offset = size / 2

	private val linearCells = view map translate

	private def toLinearIdx(p: Pos) = (p.y + offset) * size + (p.x + offset)

	private def fromLinearIdx(idx: Int) = Pos(idx % size - offset, idx / size - offset)

	def apply(p: Pos) = linearCells(toLinearIdx(p))

	val allOfType: Map[Cell, Set[Pos]] =
		linearCells.zipWithIndex.
			foldLeft(Map[Cell, Set[Pos]]().withDefault(_ => Set[Pos]())) {
			case (positionsByCell, (cell, linearIdx)) =>
				val updatedPositions = positionsByCell(cell) + fromLinearIdx(linearIdx)
				positionsByCell.updated(cell, updatedPositions)
		}

	def closestOfType(cell: Cell) = bestMatchingBy(cell, -Pos.Mid.distance(_))

	def furthestOfType(cell: Cell) = bestMatchingBy(cell, Pos.Mid.distance)

	private def bestMatchingBy[B](cell: Cell, weightFun: Pos => B)(implicit cmp: Ordering[B]): Option[Pos] =
		allOfType(cell).toSeq match {
			case Seq() => None
			case list => Some(list.maxBy(weightFun))
		}

	private def translate(c: Char): Cell = {
		c match {
			case '?' => Unknown
			case '_' => Empty
			case 'W' => Wall
			case 'M' => MyBot
			case 'm' => EnemyBot
			case 'S' => MyMiniBot
			case 's' => EnemyMiniBot
			case 'P' => Zugar
			case 'p' => Toxifera
			case 'B' => Fluppet
			case 'b' => Snorg
		}
	}

	override def toString = view.grouped(size).mkString("\n")
}

