package uk.gov.justice.digital.hmpps.sentenceplan.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveRepository
import uk.gov.justice.digital.hmpps.sentenceplan.exception.NotFoundException
import uk.gov.justice.digital.hmpps.sentenceplan.model.Action
import uk.gov.justice.digital.hmpps.sentenceplan.model.ActionList
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateAction
import uk.gov.justice.digital.hmpps.sentenceplan.model.toModel
import java.util.UUID

@Service
class ActionService(
  private val objectiveRepository: ObjectiveRepository,
  private val actionRepository: ActionRepository,
) {
  fun createAction(sentencePlanId: UUID, objectiveId: UUID, action: CreateAction): Action {
    val objective = objectiveRepository.findById(objectiveId)
      .orElseThrow { throw NotFoundException("Objective", "id", objectiveId) }
    val actionEntity = ActionEntity(
      objective,
      action.description,
      action.interventionParticipation,
      action.interventionName,
      action.interventionType,
      action.status,
      action.individualOwner,
      action.practitionerOwner,
      action.otherOwner,
      action.targetDateMonth,
      action.targetDateYear,
    )

    actionRepository.save(actionEntity)
    return actionEntity.toModel()
  }

  fun updateAction(sentencePlanId: UUID, objectiveId: UUID, id: UUID, action: Action): Action {
    if (!objectiveRepository.existsById(objectiveId)) throw NotFoundException("Objective", "id", objectiveId)
    val actionEntity = actionRepository.findByIdOrNull(id) ?: throw NotFoundException("action", "id", id)
    actionEntity.description = action.description
    actionEntity.interventionName = action.interventionName
    actionEntity.interventionType = action.interventionType
    actionEntity.status = action.status
    actionEntity.individualOwner = action.individualOwner
    actionEntity.practitionerOwner = action.practitionerOwner
    actionEntity.otherOwner = action.otherOwner
    actionEntity.targetDateMonth = action.targetDateMonth
    actionEntity.targetDateYear = action.targetDateYear
    return actionRepository.save(actionEntity).toModel()
  }

  fun deleteAction(sentencePlanId: UUID, objectiveId: UUID, id: UUID) {
    if (!objectiveRepository.existsById(objectiveId)) throw NotFoundException("Objective", "id", objectiveId)
    actionRepository.deleteById(id)
  }

  fun listActions(sentencePlanId: UUID, objectiveId: UUID): ActionList {
    if (!objectiveRepository.existsById(objectiveId)) throw NotFoundException("Objective", "id", objectiveId)
    return ActionList(actionRepository.findAllByObjectiveIdOrderByCreatedDateTimeAsc(objectiveId).map { it.toModel() })
  }

  fun findAction(sentencePlanId: UUID, objectiveId: UUID, id: UUID): Action {
    if (!objectiveRepository.existsById(objectiveId)) throw NotFoundException("Objective", "id", objectiveId)
    return actionRepository.findByIdOrNull(id)?.toModel()
      ?: throw NotFoundException("action", "id", id)
  }
}
