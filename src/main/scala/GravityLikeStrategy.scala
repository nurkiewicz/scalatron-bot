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

	abstract override def react(input: React): Seq[OutputOpcode] = {
		import Strategy._
		val allForces = for {
			cellType <- Cell.NonEmptyTypes
			forces <- resultantForcesOf(input.view, forceFactorOf(cellType, input), cellType)
		} yield forces

		if(input.generation > 0) {
			val forceToMaster = (input.age - 10).min(300)
			val masterForce = input.masterPos * forceToMaster
			Move(resultantDirectionForForces(allForces + masterForce + currentGlobalGravityForce(input.time, 500, 30))) +: super.react(input)
		} else {
			Move(resultantDirectionForForces(allForces + currentGlobalGravityForce(input.time, 100000, 300))) +: super.react(input)
		}
	}
}
