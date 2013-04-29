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
