package uk.gov.justice.digital.hmpps.sentenceplan.model

import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionEntity
import java.util.UUID

data class ActionList(
  val actions: List<Action>,
)

data class Action(
  val id: UUID,
  val objectiveId: UUID,
  val description: String,
  val interventionParticipation: Boolean,
  val nationalInterventionCode: String?,
  val accreditedProgramme: String?,
  val localIntervention: String?,
  val status: String,
  val owner: String,
)

data class CreateAction(
  val description: String,
  val interventionParticipation: Boolean,
  val nationalInterventionCode: String?,
  val accreditedProgramme: String?,
  val localIntervention: String?,
  val status: String,
  val owner: String,
)

fun ActionEntity.toModel() = Action(
  id,
  objectiveId,
  description,
  interventionParticipation,
  nationalInterventionCode,
  accreditedProgramme,
  localIntervention,
  status,
  owner,
)
