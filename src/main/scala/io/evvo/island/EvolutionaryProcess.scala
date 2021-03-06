package io.evvo.island

import io.evvo.agent.AgentStatus
import io.evvo.island.population.ParetoFrontier

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/** `EvolutionaryProcess` is a generic interface for evolutionary problem solvers. */
trait EvolutionaryProcess[Sol] {

  /** Run this island, until the specified termination criteria is met. This call will block
    * until the termination criteria is completed.
    *
    * @param stopAfter Specifies when to stop running the island.
    */
  def runBlocking(stopAfter: StopAfter): Unit

  /** Starts this island running, then immediately returns.
    *
    * @param stopAfter Specifies when to stop running the island.
    * @return A future that resolves after the island is done running.
    */
  def runAsync(stopAfter: StopAfter): Future[Unit]

  /** @return the current pareto frontier of solutions on this island
    */
  def currentParetoFrontier(): ParetoFrontier[Sol]

  def immigrate(): Unit
  def emigrate(): Unit

  /** @return The status of every agent that is part of this evolutionary process.
    */
  def agentStatuses(): Seq[AgentStatus]

  /** Provides a set of solutions to be added to the population of an EvolutionaryProcess.
    *
    * @param solutions the solutions to add
    */
  def addSolutions(solutions: Seq[Sol]): Unit
}

/** Defines how long an evolutionary process should be run for.
  *
  * @param time Specifies a maximum duration to run, for example, `1.second` will stop running
  *             the evolutionary process after one second.
  */
case class StopAfter(time: Duration)
