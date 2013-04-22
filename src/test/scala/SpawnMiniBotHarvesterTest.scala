/**
 * @author Tomasz Nurkiewicz
 * @since 4/21/13, 7:49 PM
 */
class SpawnMiniBotHarvesterTest extends AbstractBaseTestCase {

	feature("Harvester mini-bot") {

		val strategy = new SpawnMiniBotHarvester {}

		scenario("Should not send harvester mini-bot when too few fluppets") {
			Given("")
			val v = buildView(
				"""
				  |___B___
				  |___B___
				  |_______
				  |___M___
				  |_______
				  |_______
				  |_______
				""")

			When("")
			val outputOpcodes = strategy.react(react(v))

			Then("")
			outputOpcodes should equal (Nil)
		}

		scenario("Should send harvester mini-bot when many fluppets") {
			Given("")
			val v = buildView(
				"""
				  |__BBB__
				  |___B___
				  |_______
				  |___M___
				  |_______
				  |_______
				  |_______
				""")

			When("")
			val outputOpcodes = strategy.react(react(v))

			Then("")
			outputOpcodes should equal (Seq(Spawn(Direction.North)))
		}

		scenario("Should not send mini-bot when already one on its way") {
			Given("")
			val v = buildView(
				"""
				  |__BBB__
				  |___BB__
				  |___S___
				  |___M___
				  |_______
				  |_______
				  |_______
				""")

			When("")
			val outputOpcodes = strategy.react(react(v))

			Then("")
			outputOpcodes should equal (Nil)
		}

	}
}
