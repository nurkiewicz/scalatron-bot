sealed trait InputOpcode
case class Welcome(name: String, apocalypse: Int, round: Int) extends InputOpcode
case class React(generation: Int, time: Int, view: View, energy: Long, name: String, masterPos: Pos) extends InputOpcode {
	def age = if(generation == 0) time else time - name.toInt
}

case class Goodbye(energy: Int) extends InputOpcode
case class RawOpcode(name: String, params: Map[String, String])



