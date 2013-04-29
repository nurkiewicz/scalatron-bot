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
