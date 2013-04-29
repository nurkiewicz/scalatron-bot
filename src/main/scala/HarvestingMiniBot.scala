trait HarvestingMiniBot extends GravityLikeStrategy {
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
