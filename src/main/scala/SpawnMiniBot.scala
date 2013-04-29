trait SpawnMiniBot extends Strategy {

	abstract override def react(input: React) = {
		if(shouldSpawn(input))
			spawnMiniBotInNonConflictingDirection(input)
		else
			super.react(input)
	}

	def shouldSpawn(input: React) =
		input.energy > 100 &&
			(input.time < 100 || spawningPaysOff(input.view))

	val CellTypeSpawnInfluence = Map(
		Zugar -> 3,
		Fluppet -> 4,
		EnemyMiniBot -> 30,
		EnemyBot -> 4,
		MyMiniBot -> -10,
		MyBot -> -2
	)

	def spawningPaysOff(view: View) =
		CellTypeSpawnInfluence.map{case(cellType, influence) => view.allOfType(cellType).size * influence}.sum > 0

	private def spawnMiniBotInNonConflictingDirection(input: React): Seq[OutputOpcode] = {
		val slaveName = input.time.toString
		super.react(input) flatMap {
			case m@Move(mainBotDir) => Seq(m, Spawn(mainBotDir.turnBack, slaveName))
			case x => Seq(x)
		}
	}
}
