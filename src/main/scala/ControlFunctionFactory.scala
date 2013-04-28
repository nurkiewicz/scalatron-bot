class MasterStrategy extends BotStrategy with AvoidObstacles with GoToAnyNearestGoodCell with SpawnMiniBotHarvester

class SlaveStrategy extends FollowEnemyMiniBot with AvoidObstacles with GoToAnyNearestGoodCell with ExplodeCloseToEnemy with SpawnMiniBotHarvester with ExplodeWhenLowEnergy

trait BotStrategy extends GravityLikeStrategy {
	override def forceFactorOf(cell: Cell, input: React) = cell match {
		case Unknown => 0
		case Empty =>  0
		case Wall => -10
		case MyBot => 0
		case EnemyBot => -80
		case MyMiniBot => 10
		case EnemyMiniBot => -120
		case Zugar => 10
		case Toxifera => -10
		case Fluppet => 8
		case Snorg => -3
	}

}

trait FollowEnemyMiniBot extends GravityLikeStrategy {
	override def forceFactorOf(cell: Cell, input: React) = cell match {
		case Unknown => 0
		case Empty =>  0
		case Wall => -0.5
		case MyBot => (input.age - 10) * 20
		case EnemyBot => 120
		case MyMiniBot => -10
		case EnemyMiniBot => 80
		case Zugar => 25
		case Toxifera => -1
		case Fluppet => 20
		case Snorg => -1
	}
}

class GeneralStrategy(masterStrategy: Strategy, slaveStrategy: Strategy) extends Strategy {
	override def react(input: React) =
		input.generation match {
			case 0 => masterStrategy.react(input)
			case _ => slaveStrategy.react(input)
		}
}

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

trait MoveChangingStrategyDecorator extends Strategy {
	abstract override def react(input: React) = {
		val resultOpcodes = super.react(input)
		resultOpcodes collect {
			case m: Move => changeMove(input.view, m)
			case x => x
		}
	}

	def changeMove(view: View, original: Move) = original

}

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

trait ExplodeCloseToEnemy extends Strategy {
	abstract override def react(input: React) = {
		val view = input.view
		val closestEnemy = Seq(view.closestOfType(EnemyMiniBot), view.closestOfType(EnemyBot)).flatten.headOption
		closestEnemy match {
			case Some(pos) if Pos.Mid.euclideanDistance(pos) <= 2 => Seq(Explode(3))
			case _ => super.react(input)
		}
	}
}

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

trait ExplodeWhenLowEnergy extends Strategy {

	val InterestingCells = Cell.NonEmptyTypes - MyMiniBot - Wall

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

trait SpawnMiniBotHarvester extends Strategy {

	abstract override def react(input: React) = {
		if(input.slaves < 32)
			spawnMiniBotInNonConflictingDirection(input)
		else
			super.react(input)
	}

	private def spawnMiniBotInNonConflictingDirection(input: React): Seq[OutputOpcode] = {
		if(input.energy > 100) {
			val slaveName = input.time.toString
			super.react(input) flatMap {
				case m@Move(mainBotDir) => Seq(m, Spawn(mainBotDir.turnBack, slaveName))
				case x => Seq(x)
			}
		} else {
			super.react(input)
		}
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

// --------- FRAMEWORK CODE ----------

sealed trait OutputOpcode

case class Move(direction: Direction) extends OutputOpcode {
	override def toString = "Move(direction=" + direction + ")"
}

case class Explode(size: Int) extends OutputOpcode {
	override def toString = "Explode(size=" + size + ")"
}

case class Spawn(direction: Direction, name: String, energy: Int = 100) extends OutputOpcode {
	override def toString = "Spawn(direction=" + direction + ",name=" + name + ",energy=" + energy + ")"
}

case class Status(text: String) extends OutputOpcode {
	override def toString = "Status(text=" + text + ")"
}

case class Say(text: String) extends OutputOpcode {
	override def toString = "Say(text=" + text + ")"
}

sealed trait InputOpcode
case class Welcome(name: String, apocalypse: Int, round: Int) extends InputOpcode
case class React(generation: Int, time: Int, view: View, energy: Long, slaves: Int, name: String, masterPos: Pos) extends InputOpcode {
	def age = if(generation == 0) time else time - name.toInt
}

case class Goodbye(energy: Int) extends InputOpcode
case class RawOpcode(name: String, params: Map[String, String])

object CommandParser {
	val opcode = """(\w+)\((.*)\)""".r

	def parseRaw(input: String) = {
		val opcode(name, params) = input
		val paramPairs = params.split(",").map {
			param =>
				val kv = param.split("=")
				(kv(0) -> kv(1))
		}.toMap
		RawOpcode(name, paramPairs)
	}

	def fromRawOpcode(raw: RawOpcode) = raw.name match {
		case "Welcome" => Welcome(
			raw.params("name"),
			raw.params("apocalypse").toInt,
			raw.params("round").toInt)
		case "React" => React(
			raw.params("generation").toInt,
			raw.params("time").toInt,
			new View(raw.params("view")),
			raw.params("energy").toLong,
			raw.params.get("slaves").map(_.toInt).getOrElse(0),
			raw.params("name"),
			raw.params.get("master").map(Pos.apply).getOrElse(Pos.Mid)
		)
		case "Goodbye" => Goodbye(raw.params("energy").toInt)
	}

	def apply(input: String) = fromRawOpcode(parseRaw(input))
}

object Cell {
	val AllTypes = Set[Cell](Unknown, Empty, Wall, MyBot, EnemyBot, MyMiniBot, EnemyMiniBot, Zugar, Toxifera, Fluppet, Snorg)
	val NonEmptyTypes = AllTypes - Unknown - Empty
}

sealed trait Cell
case object Unknown extends Cell
case object Empty extends Cell
case object Wall extends Cell
case object MyBot extends Cell
case object EnemyBot extends Cell
case object MyMiniBot extends Cell
case object EnemyMiniBot extends Cell
case object Zugar extends Cell
case object Toxifera extends Cell
case object Fluppet extends Cell
case object Snorg extends Cell

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

case object Direction {
	private val dict = Vector(Pos(1, 0), Pos(1, 1), Pos(0, 1), Pos(-1, 1), Pos(-1, 0), Pos(-1, -1), Pos(0, -1), Pos(1, -1))

	private def angle(p: Pos) = (((math.atan2(p.y, p.x) * 180 / math.Pi + 360) % 360) / 45).toInt

	def apply(pos: Pos): Direction = if(pos != Pos.Mid)
		Direction.apply(angle(pos) % 8)
	else {
		Direction.apply((math.random * 8).toInt)
	}

	val East = new Direction(0)
	val SouthEast = new Direction(1)
	val South = new Direction(2)
	val SouthWest = new Direction(3)
	val West = new Direction(4)
	val NorthWest = new Direction(5)
	val North = new Direction(6)
	val NorthEast = new Direction(7)

	val All = Seq(East, SouthEast, South, SouthWest, West, NorthWest, North, NorthEast)
}

case class Direction(dirIdx: Int) {
	val offset = Direction.dict(dirIdx)
	override def toString = offset.x + ":" + offset.y
	def rotateLittleBitLeft = Direction((dirIdx + 7) % 8)
	def rotateLittleBitRight = Direction((dirIdx + 1) % 8)
	def turnBack = Direction((dirIdx + 4) % 8)
}

class View(view: String) {
	val size = math.sqrt(view.size).toInt
	private val offset = Pos(size / 2, size / 2)

	val cells = view.map(translate).grouped(size).map(_.toIndexedSeq).toIndexedSeq

	def apply(p: Pos) = {
		val withOffset = p + offset
		getAbsolute(withOffset.x, withOffset.y)
	}

	private def getAbsolute(x: Int, y: Int) = {
		val maybeCell = for {
			row <- safeGet(cells, y)
			cell <- safeGet(row, x)
		} yield cell
		maybeCell getOrElse Unknown
	}

	def allOfType(cell: Cell) = for {
		y <- 0 until size
		x <- 0 until size
		if (getAbsolute(x, y) == cell)
	} yield Pos(x, y) - offset

	def closestOfType(cell: Cell) = bestMatchingBy(cell, -Pos.Mid.distance(_))

	def furthestOfType(cell: Cell) = bestMatchingBy(cell, Pos.Mid.distance)

	private def bestMatchingBy[B](cell: Cell, weightFun: Pos => B)(implicit cmp: Ordering[B]): Option[Pos] =
		allOfType(cell) match {
			case Seq() => None
			case list => Some(list.maxBy(weightFun))
		}

	private def safeGet[T](seq: Seq[T], index: Int) =
		if (index >= 0 && index < seq.size)
			Some(seq(index))
		else
			None

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

