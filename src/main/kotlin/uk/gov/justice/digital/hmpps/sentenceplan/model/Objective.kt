package uk.gov.justice.digital.hmpps.sentenceplan.model

import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveEntity
import java.util.UUID

data class Objective(
  val id: UUID,
  val sentencePlanId: UUID,
  val description: String,
  val needs: Set<Need>,
)

data class CreateObjective(
  val description: String,
  val needs: Set<Need>,
)

data class Need(
  val code: String,
  val id: UUID? = null,
)

fun ObjectiveEntity.toModel() = Objective(
  id = id,
  sentencePlanId = sentencePlan.id,
  description = description,
  needs = this.needs.map { Need(it.code, it.id) }.toSet(),
)
