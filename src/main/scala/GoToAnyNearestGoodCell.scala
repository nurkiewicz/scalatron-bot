trait GoToAnyNearestGoodCell extends MoveChangingStrategyDecorator {
	private val GoodCells = Set[Cell](Zugar, Fluppet)

	abstract override def changeMove(view: View, original: Move): Move = {
		val changedMove = super.changeMove(view, original)
		val cellAfterOriginalMove = view(Pos.Mid + changedMove.direction)
		if(cellAfterOriginalMove == Empty || cellAfterOriginalMove == Unknown) {
			val maybeAlternativeMove = for {
				alternativeMove <- Direction.All
				if(alternativeMove != changedMove.direction)
				if(GoodCells contains view(Pos.Mid + alternativeMove))
			} yield Move(alternativeMove)
			maybeAlternativeMove.headOption.getOrElse(changedMove)
		} else {
			changedMove
		}
	}
}
