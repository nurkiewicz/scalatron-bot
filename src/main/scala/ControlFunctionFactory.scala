trait StandardBehaviour extends AvoidObstacles with GoToAnyNearestGoodCell with SpawnMiniBot

class MasterStrategy extends ExploreMapStrategy with StandardBehaviour

class SlaveStrategy extends HarvestingMiniBot with StandardBehaviour with ExplodeCloseToEnemy with ExplodeWhenLowEnergy

class GeneralStrategy(masterStrategy: Strategy, slaveStrategy: Strategy) extends Strategy {
	override def react(input: React) =
		input.generation match {
			case 0 => masterStrategy.react(input)
			case _ => slaveStrategy.react(input)
		}
}

class ControlFunctionFactory {
	val strategy = new GeneralStrategy(new MasterStrategy, new SlaveStrategy)
	def create = new Bot(strategy).respond _
}

class Bot(strategy: Strategy) {

	def respond(input: String) = {
		val outputOpcodes = CommandParser(input) match {
			case r: React => strategy.react(r)
			case _ => Nil
		}
		outputOpcodes.map(_.toString).mkString("|")
	}
}
