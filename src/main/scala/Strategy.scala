object Strategy {
	private def cube(x: Double) = x * x * x

	def resultantForcesOf(view: View, forceMultiplier: Double, cell: Cell) =
		view.allOfType(cell).map{pos =>
			pos * (10000.0 * forceMultiplier / cube(pos.distance(Pos.Mid))).round.toInt
		}

	def resultantDirectionForForces(forces: TraversableOnce[Pos]) = {
		val result = forces.foldLeft(Pos.Mid)((cur, acc) => cur + acc)
		Direction(result)
	}

}

trait Strategy {
	def react(input: React): Seq[OutputOpcode] = Nil
}

