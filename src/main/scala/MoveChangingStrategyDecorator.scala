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
