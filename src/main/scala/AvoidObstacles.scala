trait AvoidObstacles extends MoveChangingStrategyDecorator {
	private val avoid = Set[Cell](Wall, Snorg, MyMiniBot)
	private def avoidObstacles(view: View, direction: Direction) = {

		def tryDirection(direction: Direction) = {
			val cellAfterMove = view(Pos.Mid + direction)
			if(!(avoid contains cellAfterMove)) {
				Some(direction)
			} else {
				None
			}
		}

		def tryMirrorDirections(left: Direction, right: Direction, max: Int): Seq[Option[Direction]] = {
			if(max <= 0)
				Nil
			else
				tryDirection(left) +:
					tryDirection(right) +:
					tryMirrorDirections(left.rotateLittleBitLeft, right.rotateLittleBitRight, max - 1)
		}

		(tryDirection(direction) +: tryMirrorDirections(direction.rotateLittleBitLeft, direction.rotateLittleBitRight, 4)).flatten
	}

	abstract override def changeMove(view: View, original: Move): Move = {
		val originalDir = super.changeMove(view, original).direction
		avoidObstacles(view, originalDir).headOption match {
			case Some(adjustedDirection) => Move(adjustedDirection)
			case None => original
		}
	}
}
