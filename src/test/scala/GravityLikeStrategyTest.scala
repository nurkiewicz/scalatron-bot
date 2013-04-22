/**
 * @author Tomasz Nurkiewicz
 * @since 4/7/13, 11:20 PM
 */
class GravityLikeStrategyTest extends AbstractBaseTestCase {
	
	val strategy = new MasterStrategy
	
	feature("Moving toward 'good' cells") {
		
		scenario("Move toward Zugar up") {
			Given("")
			val view = buildView(
				"""
				  |__P__
				  |_____
				  |__M__
				  |_____
				  |_____
				""")
		
			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.North)))
		}

		scenario("Move toward Zugar down right") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |_____
				  |__M__
				  |_____
				  |____P
				""")
		
			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.SouthEast)))
		}

		scenario("Move toward Zugar that is closer") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |__P__
				  |__M__
				  |_____
				  |____P
				""")
		
			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.North)))
		}

		scenario("Move toward Fluppet up") {
			Given("")
			val view = buildView(
				"""
				  |__B__
				  |_____
				  |__M__
				  |_____
				  |_____
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.North)))
		}

		scenario("Move toward Fluppet down right") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |_____
				  |__M__
				  |_____
				  |____B
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.SouthEast)))
		}

		scenario("Move toward Fluppet that is closer") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |__B__
				  |__M__
				  |_____
				  |____B
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.North)))
		}

		scenario("Prefer Zugar when same distance as Zugar") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |_____
				  |B_M_P
				  |_____
				  |_____
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.East)))
		}

		scenario("Prefer Zugar when same distance as Fluppet") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |_____
				  |B_MP_
				  |_____
				  |_____
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.East)))
		}

		scenario("Should take closest cell and avoid oscillating when between two Zugars") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |__P__
				  |__M__
				  |___P_
				  |_____
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.SouthEast)))
		}

	}

	feature("Running away from danger") {

		scenario("Running away from Snorg") {
			Given("")
			val view = buildView(
				"""
				  |b____
				  |_____
				  |__M__
				  |_____
				  |_____
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.SouthEast)))
		}

		scenario("Running away from Toxifera") {
			Given("")
			val view = buildView(
				"""
				  |____p
				  |_____
				  |__M__
				  |_____
				  |_____
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.SouthWest)))
		}
		
		scenario("Grab Zugar when escaping from Snorg") {
			Given("")
			val view = buildView(
				"""
				  |_______
				  |_______
				  |__P____
				  |___M___
				  |_______
				  |_______
				  |___b___
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.NorthWest)))
		}
	}

	feature("Going around walls") {

		scenario("Avoid wall when going to Zugar") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |_____
				  |__MWP
				  |_____
				  |_____
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.NorthEast)))
		}

	}

	feature("Spawn when enemy mini bot seen") {
		scenario("Spawning when one mini bot visible") {
			Given("")
			val view = buildView(
				"""
				  |_____
				  |_____
				  |__M_s
				  |_____
				  |_____
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			val spawnDirections = outputOpcodes.collect{case Spawn(dir, _, _) => dir}
			spawnDirections should equal (Seq(Direction.East))
		}

		scenario("Not spawning when mini-bot already on its way") {
			Given("")
			val view = buildView(
				"""
				  |_______
				  |_______
				  |_______
				  |___M___
				  |____S__
				  |_______
				  |_____s_
				""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			val spawnDirections = outputOpcodes.collect{case Spawn(dir, _, _) => dir}
			spawnDirections should equal (Nil)
		}

		scenario("Spawning second when one two enemy mini-bots visible but only one our bot") {
			Given("")
			val view = buildView(
			"""
			  |___s___
			  |_______
			  |_______
			  |___M___
			  |____S__
			  |_____s_
			  |_______
			""")

			When("")
			val outputOpcodes = strategy.react(react(view))

			Then("")
			val spawnDirections = outputOpcodes.collect{case Spawn(dir, _, _) => dir}
			spawnDirections should equal (Seq(Direction.North))
		}
	}

}
