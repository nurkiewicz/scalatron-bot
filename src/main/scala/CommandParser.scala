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
			raw.params("name"),
			raw.params.get("master").map(Pos.apply).getOrElse(Pos.Mid)
		)
		case "Goodbye" => Goodbye(raw.params("energy").toInt)
	}

	def apply(input: String) = fromRawOpcode(parseRaw(input))
}
