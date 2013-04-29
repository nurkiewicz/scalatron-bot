trait GravityLikeStrategy extends Strategy {

	def forceFactorOf(cell: Cell, input: React): Double

	private def currentGravityAngle(time: Int, period: Int) = (((time / period) * 13) % 8) / 8.0 * 2 * math.Pi

	private def currentGlobalGravityForce(time: Int, force: Int, period: Int) = {
		val gravityAngle = currentGravityAngle(time, period)
		Pos(
			(math.sin(gravityAngle) * force).toInt,
			(math.cos(gravityAngle) * force).toInt
		)
	}

	private def forceToMaster(input: React) = input.masterPos * (input.age - 10).min(300)

	private def forcesToCells(input: React) = for {
		cellType <- Cell.NonEmptyTypes.toSeq
		forces <- Strategy.resultantForcesOf(input.view, forceFactorOf(cellType, input), cellType)
	} yield forces

	abstract override def react(input: React): Seq[OutputOpcode] = {
		val extraGlobalForce = if(input.isMiniBot) {
			currentGlobalGravityForce(input.time, 500, 30) + forceToMaster(input)
		} else {
			currentGlobalGravityForce(input.time, 100000, 300)
		}
		Move(Strategy.resultantDirectionForForces(extraGlobalForce +: forcesToCells(input))) +: super.react(input)
	}
}
