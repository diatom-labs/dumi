package com.diatom.agent

import com.diatom.agent.func.TCreatorFunc
import com.diatom.population.TPopulation

import scala.concurrent.duration._


trait TCreatorAgent[Sol] extends TAgent[Sol]

case class CreatorAgent[Sol](creatorFunc: TCreatorFunc[Sol],
                             pop: TPopulation[Sol],
                             strat: TAgentStrategy)
  extends AAgent[Sol](strat, pop) with TCreatorAgent[Sol]{

  override def step(): Unit = {
    val toAdd = creatorFunc.create()
    pop.addSolutions(toAdd)
  }
}


object CreatorAgent {
  def from[Sol](creatorFunc: TCreatorFunc[Sol], pop: TPopulation[Sol]): TCreatorAgent[Sol] = {
    CreatorAgent.from(creatorFunc, pop, CreatorAgentDefaultStrategy())
  }

  def from[Sol](creatorFunc: TCreatorFunc[Sol], pop: TPopulation[Sol], strat: TAgentStrategy)
               : TCreatorAgent[Sol] = {
    CreatorAgent(creatorFunc, pop, strat)
  }
}

case class CreatorAgentDefaultStrategy() extends TAgentStrategy {
  override def waitTime(populationInformation: TPopulationInformation): Duration = {
    val n = populationInformation.numSolutions

    if (n > 1000) {
      1000.millis // they have 1000 solutions, they're probably set.
    } else {
      math.max(1, math.exp(.001 * n)).millis
    }
  }
}