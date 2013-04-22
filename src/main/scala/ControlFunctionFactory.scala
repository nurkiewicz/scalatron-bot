class MasterStrategy extends BotStrategy with AvoidObstacles with GoToAnyNearestGoodCell with SpawnMiniBotInDefence with SpawnMiniBotHarvester

class SlaveStrategy extends FollowEnemyMiniBot with AvoidObstacles with GoToAnyNearestGoodCell with ExplodeCloseToEnemy

trait BotStrategy extends GravityLikeStrategy {
	override def forceFactorOf(cell: Cell, input: React) = cell match {
		case Unknown => 0
		case Empty =>  0
		case Wall => -1
		case MyBot => 0
		case EnemyBot => -80
		case MyMiniBot => 0
		case EnemyMiniBot => -120
		case Zugar => 55
		case Toxifera => -5
		case Fluppet => 50
		case Snorg => -5
	}

}

trait FollowEnemyMiniBot extends GravityLikeStrategy {
	override def forceFactorOf(cell: Cell, input: React) = cell match {
		case Unknown => 0
		case Empty =>  0
		case Wall => -1
		case MyBot => -2 + input.energy / 100.0     //more energy -> more eager to join parent bot
		case EnemyBot => 80
		case MyMiniBot => -1
		case EnemyMiniBot => 120
		case Zugar => 25
		case Toxifera => -4
		case Fluppet => 20
		case Snorg => -4
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

	abstract override def react(input: React): Seq[OutputOpcode] = {
		import Strategy._
		val allForces = for {
			cellType <- Cell.AllTypes
			forces <- resultantForcesOf(input.view, forceFactorOf(cellType, input), cellType)
		} yield forces
		Move(resultantDirectionForForces(allForces)) +: super.react(input)
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
	private def avoidObstacles(view: View, direction: Direction) = {

		def tryDirection(direction: Direction) = {
			val cellAfterMove = view(Pos.Mid + direction)
			if(cellAfterMove != Wall && cellAfterMove != Snorg) {
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
	abstract override def changeMove(view: View, original: Move): Move = {
		val changedMove = super.changeMove(view, original)
		val cellAfterOriginalMove = view(Pos.Mid + changedMove.direction)
		if(cellAfterOriginalMove == Empty || cellAfterOriginalMove == Unknown) {
			val maybeAlternativeMove = for {
				alternativeMove <- Direction.All
				if(alternativeMove != changedMove.direction)
				if(view(Pos.Mid + alternativeMove) == Zugar || view(Pos.Mid + alternativeMove) == Fluppet)
			} yield Move(alternativeMove)
			maybeAlternativeMove.headOption.getOrElse(changedMove)
		} else {
			changedMove
		}
	}
}

trait SpawnMiniBotInDefence extends Strategy {
	abstract override def react(input: React) = {
		val view = input.view
		val enemyMiniBotCount = view.allOfType(EnemyMiniBot).size
		val myMiniBotCount = view.allOfType(MyMiniBot).size
		if(enemyMiniBotCount > myMiniBotCount) {
			spawnMiniBot(view) +: super.react(input)
		} else {
			super.react(input)
		}
	}

	private def spawnMiniBot(view: View) = {
		val enemyBotPos = view.furthestOfType(EnemyMiniBot).get   //has to be at least one
		val myMiniBotDir = Direction(enemyBotPos)
		Spawn(myMiniBotDir)
	}
}

trait SpawnMiniBotHarvester extends Strategy {
	abstract override def react(input: React) = {
		val view = input.view
		val zugarsVisible = view.allOfType(Zugar)
		val fluppetsVisible = view.allOfType(Fluppet)
		val myMiniBotsVisible = view.allOfType(MyMiniBot)
		val enoughGoodCells = zugarsVisible.size * 2 + fluppetsVisible.size * 4 - myMiniBotsVisible.size * 15
		if(enoughGoodCells >= 15) {
			spawnMiniBotInNonConflictingDirection(view, input)
		} else {
			super.react(input)
		}
	}

	private def spawnMiniBotInNonConflictingDirection(view: View, input: React): Seq[OutputOpcode] = {
		import Strategy._
		val miniBotForces = resultantForcesOf(view, 50, Fluppet) ++ resultantForcesOf(view, 20, Zugar)
		val miniBotDir = resultantDirectionForForces(miniBotForces)
		val otherOpcodes = super.react(input) map {
			case Move(mainBotDir) if (mainBotDir == miniBotDir) => Move(mainBotDir.turnBack)
			case x => x
		}
		Spawn(miniBotDir) +: otherOpcodes
	}
}

class ControlFunctionFactory {
	val strategy = new GeneralStrategy(new MasterStrategy, new SlaveStrategy)
	def create = new Bot(strategy).respond _
}

class Bot(strategy: Strategy) {

	def respond(input: String) = CommandParser(input) match {
		case r: React => strategy.react(r).map(_.toString).mkString("|")
		case _ => ""
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

case class Spawn(direction: Direction, name: String = "Slave", energy: Int = 100) extends OutputOpcode {
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
case class React(generation: Int, time: Int, view: View, energy: Long) extends InputOpcode
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
		case "Welcome" => Welcome(raw.params("name"), raw.params("apocalypse").toInt, raw.params("round").toInt)
		case "React" => React(
			raw.params("generation").toInt, 
			raw.params("time").toInt, 
			new View(raw.params("view")), 
			raw.params("energy").toLong
		)
		case "Goodbye" => Goodbye(raw.params("energy").toInt)
	}

	def apply(input: String) = fromRawOpcode(parseRaw(input))
}

object Cell {
	val AllTypes = Seq[Cell](Unknown, Empty, Wall, MyBot, EnemyBot, MyMiniBot, EnemyMiniBot, Zugar, Toxifera, Fluppet, Snorg)
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
}

case class Pos(x: Int, y: Int)  {
	def linear(size: Int) = y * size + x
	def euclideanDistance(other: Pos) = math.sqrt((other.x - x) * (other.x - x) + (other.y - y) * (other.y - y))
	def distance(other: Pos) = math.max((other.x - x).abs, (other.y - y).abs)
	def +(other: Pos) = Pos(x + other.x, y + other.y)
	def +(dir: Direction): Pos = this + dir.offset
	def -(other: Pos) = Pos(x - other.x, y - other.y)
	def *(value: Int) = Pos(x * value, y * value)

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
	else
		Direction.apply((math.random * 8).toInt)

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

