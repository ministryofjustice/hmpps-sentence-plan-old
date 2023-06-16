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
  val interventionParticipation: Boolean = false,
  val interventionName: String?,
  val interventionType: String?,
  val status: String,
  val individualOwner: Boolean = false,
  val practitionerOwner: Boolean = false,
  val otherOwner: String?,
)

data class CreateAction(
  val description: String,
  val interventionParticipation: Boolean,
  val interventionName: String?,
  val interventionType: String?,
  val status: String,
  val individualOwner: Boolean = false,
  val practitionerOwner: Boolean = false,
  val otherOwner: String?,
)

fun ActionEntity.toModel() = Action(
  id,
  objectiveId,
  description,
  interventionParticipation,
  interventionName,
  interventionType,
  status,
  individualOwner,
  practitionerOwner,
  otherOwner,
)
