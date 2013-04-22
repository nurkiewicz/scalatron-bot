import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author Tomasz Nurkiewicz
 * @since 4/7/13, 11:20 PM
 */
@RunWith(classOf[JUnitRunner])
abstract class AbstractBaseTestCase extends FeatureSpec with GivenWhenThen with ShouldMatchers {

	def buildView(s: String) = new View(s.stripMargin.filterNot(Character.isWhitespace))

	def react(view: View) = React(0, 0, view, 0)

}
