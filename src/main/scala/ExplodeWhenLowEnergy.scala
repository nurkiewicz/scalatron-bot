trait ExplodeWhenLowEnergy extends Strategy {

	val InterestingCells = Cell.NonEmptyTypes - MyMiniBot - Wall - Toxifera - Snorg

	val LowEnergyThreshold = 80

	def interestingCellsInNeighbourhood(view: View) =
		for {
			cellType <- InterestingCells
			cell <- view.allOfType(cellType)
		} yield cell

	override def react(input: React): Seq[OutputOpcode] =
		if(input.energy < LowEnergyThreshold && interestingCellsInNeighbourhood(input.view).isEmpty) {
			Seq(Explode(10))
		} else
			super.react(input)
}
