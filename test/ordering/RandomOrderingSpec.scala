package ordering

import org.scalacheck.{Gen, Prop}
import org.scalatest.WordSpec
import org.scalatest.prop.{Checkers, GeneratorDrivenPropertyChecks}
import play.api.Logger
import rendering.VersionItem
import support.OrderingCheck

import scala.io.Source

class RandomOrderingSpec extends WordSpec with GeneratorDrivenPropertyChecks with Checkers with OrderingCheck {

  "regex string ordering" should {

    val candidateVersions = Source.fromFile(s"test/resources/candidates.csv")
      .getLines
      .toList
      .map((line: String) => line.split(",").head -> line.split(",").tail.toList)
      .toMap[String, List[String]]

    "accurately order actual versions" in new VersionItemOrdering {

      candidateVersions.foreach { case (candidate, versions) =>

        val fullExpected = versions.map(VersionItem(_))

        check {
          Prop.forAll(Gen.pick(fullExpected.length - 1, fullExpected.toSet).map(_.toList)) { xs: List[VersionItem] =>

            val partialActual = xs.ascendingOrder

            Logger.info(s"$candidate shuffled: ${xs.map(_.version).mkString(",")} ===> ordered: ${partialActual.map(_.version).mkString(",")}")

            orderCheck(partialActual, fullExpected)
          }
        }
      }
    }
  }
}