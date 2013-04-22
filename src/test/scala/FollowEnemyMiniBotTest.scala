/**
 * @author Tomasz Nurkiewicz
 * @since 4/20/13, 9:44 PM
 */
class FollowEnemyMiniBotTest extends AbstractBaseTestCase {

	feature("Following enemy mini bot") {

		val strategy = new FollowEnemyMiniBot {}

		scenario("Should follow enemy mini bot") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |_______
				  |_______
				  |___M___
				  |_______
				  |_______
				  |__s____
				""")

			When("")
			val outputOpcodes = strategy.react(react(v))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.South)))
		}

		scenario("Should follow closest enemy mini bot when multiple visible") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |___s___
				  |_______
				  |___M___
				  |_______
				  |_______
				  |___s___
				""")

			When("")
			val outputOpcodes = strategy.react(react(v))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.North)))
		}

		scenario("Should go to Zugar when nothing else visible") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |_______
				  |_______
				  |___M__P
				  |_______
				  |_______
				  |_______
				""")

			When("")
			val outputOpcodes = strategy.react(react(v))

			Then("")
			outputOpcodes should equal (Seq(Move(Direction.East)))
		}

	}

	feature("Explode close to enemy") {

		val strategy = new SlaveStrategy

		scenario("Should explode close to enemy mini bot") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |_______
				  |____s__
				  |___M___
				  |_______
				  |_______
				  |_______
				""")

			When("")
			val outputOpcodes = strategy.react(react(v))

			Then("")
			outputOpcodes should equal (Seq(Explode(3)))
		}

		scenario("Should explode close to enemy bot") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |_______
				  |____m__
				  |___M___
				  |_______
				  |_______
				  |_______
				""")

			When("")
			val outputOpcodes = strategy.react(react(v))

			Then("")
			outputOpcodes should equal (Seq(Explode(3)))
		}

	}
}
