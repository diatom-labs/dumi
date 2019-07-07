package com.evvo.island

import com.evvo.NullLogger
import com.evvo.island.population.{Maximize, Objective, Scored}
import com.evvo.tags.Slow
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.concurrent.duration._

class EvvoIslandTest extends WordSpec with Matchers with BeforeAndAfter {
  implicit val log = NullLogger

  object MaximizeInt extends Objective[Int]("Test", Maximize) {
    override protected def objective(sol: Int): Double = sol
  }

  // private because EvvoIsland is private, required to compile.
  private var island1: EvvoIsland[Int] = _
  private var island2: EvvoIsland[Int] = _

  before {
    island1 = new EvvoIsland(
      Vector(),
      Vector(),
      Vector(),
      Vector(MaximizeInt),
      ElitistImmigrationStrategy,
      RandomSampleEmigrationStrategy(4)
    )

    island2 = new EvvoIsland(
      Vector(),
      Vector(),
      Vector(),
      Vector(MaximizeInt),
      ElitistImmigrationStrategy,
      RandomSampleEmigrationStrategy(4)
    )
  }

  "EvvoIsland" should {
    "use immigration strategy to filter incoming solutions" in {
      island1.immigrate(Seq(
        Scored[Int](Map(("Test", Maximize) -> 10), 10),
        Scored[Int](Map(("Test", Maximize) -> 3), 3)))

      // The three shouldn't be added, because Elitist will prevent anything < 10 from being added
      island1.currentParetoFrontier().solutions should have size 1

      // But 11 should make it through.
      val solution11 = Scored[Int](Map(("Test", Maximize) -> 11), 11)
      island1.immigrate(Seq(solution11))
      island1.currentParetoFrontier().solutions should be(Set(solution11))
    }

    "emigrate strategies to other islands" in(Slow, {
      island1.registerIslands(Seq(island2))

      island1.immigrate(Seq(Scored[Int](Map(("Test", Maximize) -> 10), 10)))

      // Is island2 is changed by island 1 running, then emigration must have happened.
      island2.currentParetoFrontier().solutions.size shouldBe 0
      island1.runBlocking(StopAfter(1.second))
      island2.currentParetoFrontier().solutions.size shouldBe 1
    })

    "use the emigration strategy to choose which solutions to emigrate" in(Slow, {
      val noEmigrationIsland = new EvvoIsland[Int](
        Vector(),
        Vector(),
        Vector(),
        Vector(MaximizeInt),
        ElitistImmigrationStrategy,
        NoEmigrationEmigrationStrategy
      )

      noEmigrationIsland.registerIslands(Seq(island2))

      // Because island2 wasn't changed, in conjunction with the above test, the change in
      // emigration strategy made a difference.
      island2.currentParetoFrontier().solutions.size shouldBe 0
      noEmigrationIsland.runBlocking(StopAfter(1.second))
      island2.currentParetoFrontier().solutions.size shouldBe 0

    })
  }

}
