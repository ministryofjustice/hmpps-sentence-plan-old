package uk.gov.justice.digital.hmpps.sentenceplan.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.getBySentencePlanIdAndId
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
    objectiveRepository.getBySentencePlanIdAndId(sentencePlanId, objectiveId)
    val actionEntity = ActionEntity(
      objectiveId,
      action.description,
      action.interventionParticipation,
      action.nationalInterventionCode,
      action.accreditedProgramme,
      action.localIntervention,
      action.status,
      action.owner,
    )

    actionRepository.save(actionEntity)
    return actionEntity.toModel()
  }

  fun updateAction(sentencePlanId: UUID, objectiveId: UUID, id: UUID, action: Action): Action {
    objectiveRepository.getBySentencePlanIdAndId(sentencePlanId, objectiveId)
    val actionEntity = actionRepository.findByIdOrNull(id) ?: throw NotFoundException("action", "id", id)
    actionEntity.description = action.description
    actionEntity.accreditedProgramme = action.accreditedProgramme
    actionEntity.localIntervention = action.localIntervention
    actionEntity.status = action.status
    actionEntity.nationalInterventionCode = action.nationalInterventionCode
    actionEntity.owner = action.owner
    return actionRepository.save(actionEntity).toModel()
  }

  fun deleteAction(sentencePlanId: UUID, objectiveId: UUID, id: UUID) {
    objectiveRepository.getBySentencePlanIdAndId(sentencePlanId, objectiveId)
    actionRepository.deleteById(id)
  }

  fun listActions(sentencePlanId: UUID, objectiveId: UUID): ActionList {
    objectiveRepository.getBySentencePlanIdAndId(sentencePlanId, objectiveId)
    return ActionList(actionRepository.findAllByObjectiveIdOrderByCreatedDateTimeAsc(objectiveId).map { it.toModel() })
  }

  fun findAction(sentencePlanId: UUID, objectiveId: UUID, id: UUID): Action {
    objectiveRepository.getBySentencePlanIdAndId(sentencePlanId, objectiveId)
    return actionRepository.findByIdOrNull(id)?.toModel()
      ?: throw NotFoundException("action", "id", id)
  }
}
