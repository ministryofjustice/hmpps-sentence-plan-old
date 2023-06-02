package uk.gov.justice.digital.hmpps.sentenceplan.model

import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveEntity
import java.util.UUID

data class Objective(
  val id: UUID,
  val sentencePlanId: UUID,
  val description: String,
)

data class CreateObjective(
  val description: String,
)

fun ObjectiveEntity.toModel() = Objective(
  id = id,
  sentencePlanId = sentencePlan.id,
  description = description,
)
