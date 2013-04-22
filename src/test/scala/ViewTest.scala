import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Tomasz Nurkiewicz
 * @since 4/7/13, 10:45 PM
 */
class ViewTest extends AbstractBaseTestCase {

	feature("Parsing view") {

		scenario("Parsing all possible cells") {
			Given("")

			When("")
			val v = buildView(
				"""
				  |?_W_m
				  |____W
				  |p_M__
				  |____b
				  |SsPpB
				""")

			Then("")
			v(Pos(-2, -2)) should equal (Unknown)
			v(Pos(2, -2)) should equal (EnemyBot)
			v(Pos(0, 0)) should equal (MyBot)
			v(Pos(2, 1)) should equal (Snorg)
			v.allOfType(Wall) should equal (Seq(Pos(0, -2), Pos(2, -1)))
			v.allOfType(Toxifera) should equal (Seq(Pos(-2, 0), Pos(1, 2)))
		}

	}

	feature("Looking for furthest and closest cells of type") {
		scenario("Finding closest Zugar when only one exists") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |__P____
				  |_______
				  |___M___
				  |_______
				  |_______
				  |_______
				""")

			When("")
			val found = v.closestOfType(Zugar)

			Then("")
			found should equal (Some(Pos(-1, -2)))
		}

		scenario("Finding furthest Zugar when only one exists") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |__P____
				  |_______
				  |___M___
				  |_______
				  |_______
				  |_______
				""")

			When("")
			val found = v.furthestOfType(Zugar)

			Then("")
			found should equal (Some(Pos(-1, -2)))
		}

		scenario("Finding closest Fluppet when multiple exist") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |__P____
				  |_______
				  |___M___
				  |_B_____
				  |_______
				  |_____B_
				""")

			When("")
			val found = v.closestOfType(Fluppet)

			Then("")
			found should equal (Some(Pos(-2, 1)))
		}

		scenario("Finding furthest Fluppet when multiple exist") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |__P____
				  |_______
				  |___M___
				  |_B_____
				  |_______
				  |_____B_
				""")

			When("")
			val found = v.furthestOfType(Fluppet)

			Then("")
			found should equal (Some(Pos(2, 3)))
		}

		scenario("Returning nothing when no furthest Zugar at all") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |_______
				  |_______
				  |___M___
				  |_B_____
				  |_______
				  |_____B_
				""")

			When("")
			val found = v.furthestOfType(Zugar)

			Then("")
			found should equal (None)
		}

		scenario("Returning nothing when no closest Zugar at all") {
			Given("")
			val v = buildView(
				"""
				  |_______
				  |_______
				  |_______
				  |___M___
				  |_B_____
				  |_______
				  |_____B_
				""")

			When("")
			val found = v.closestOfType(Zugar)

			Then("")
			found should equal (None)
		}
	}

}
