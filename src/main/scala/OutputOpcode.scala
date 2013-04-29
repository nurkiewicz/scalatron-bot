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

