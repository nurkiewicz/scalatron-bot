trait ExploreMapStrategy extends GravityLikeStrategy {
	override def forceFactorOf(cell: Cell, input: React) = cell match {
		case Unknown => 0
		case Empty =>  0
		case Wall => -10
		case MyBot => 0
		case EnemyBot => -80
		case MyMiniBot => 1
		case EnemyMiniBot => -120
		case Zugar => 10
		case Toxifera => -10
		case Fluppet => 8
		case Snorg => -3
	}

}
