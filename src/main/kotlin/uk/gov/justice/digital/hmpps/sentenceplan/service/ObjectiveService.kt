package uk.gov.justice.digital.hmpps.sentenceplan.service

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.sentenceplan.entity.NeedEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.NeedRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.getByIdOrThrow
import uk.gov.justice.digital.hmpps.sentenceplan.entity.getBySentencePlanIdAndId
import uk.gov.justice.digital.hmpps.sentenceplan.exception.NotFoundException
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateObjective
import uk.gov.justice.digital.hmpps.sentenceplan.model.Objective
import uk.gov.justice.digital.hmpps.sentenceplan.model.ObjectiveList
import uk.gov.justice.digital.hmpps.sentenceplan.model.toModel
import java.util.UUID

@Service
class ObjectiveService(
  private val objectiveRepository: ObjectiveRepository,
  private val needRepository: NeedRepository,
  private val sentencePlanRepository: SentencePlanRepository,
) {

  /**
   * Create a new objective for a sentence plan.
   *
   */
  @Transactional
  fun createObjective(sentencePlanId: UUID, objective: CreateObjective): Objective {
    val sentencePlan = sentencePlanRepository.getByIdOrThrow(sentencePlanId)
    val objectiveEntity = ObjectiveEntity(
      sentencePlan,
      description = objective.description,
      motivation = objective.motivation,
    )
    objectiveRepository.save(objectiveEntity)
    val needEntities = needRepository.saveAll(objective.needs.map { NeedEntity(code = it.code, objective = objectiveEntity) }).toSet()
    objectiveEntity.addNeeds(needEntities)
    return objectiveEntity.toModel()
  }

  @Transactional
  fun updateObjective(sentencePlanId: UUID, objective: Objective): Objective {
    val original = objectiveRepository.getBySentencePlanIdAndId(sentencePlanId, objective.id)
    val newNeedEntities = objective.needs.filter { original.needs.none { on -> it.code == on.code } }
      .map { NeedEntity(it.code, original) }.toSet()
    val removedNeeds = original.needs.filter { objective.needs.none { on -> it.code == on.code } }.toSet()
    original.description = objective.description
    original.motivation = objective.motivation
    original.addNeeds(newNeedEntities)
    original.removeNeeds(removedNeeds)
    needRepository.saveAll(newNeedEntities)
    needRepository.deleteAll(removedNeeds)

    return objectiveRepository.save(original).toModel()
  }

  /**
   * Get all existing objectives for a sentence plan.
   *
   */
  fun listObjectives(sentencePlanId: UUID) = ObjectiveList(
    objectives = sentencePlanRepository.getByIdOrThrow(sentencePlanId)
      .let { sentencePlan -> objectiveRepository.findBySentencePlanId(sentencePlan.id) }
      .map { it.toModel() },
  )

  fun findObjective(id: UUID): Objective = objectiveRepository.findByIdOrNull(id)?.toModel()
    ?: throw NotFoundException("Objective", "id", id)

  fun deleteObjective(id: UUID) {
    val objective = objectiveRepository.findByIdOrNull(id)
      ?: throw NotFoundException("Objective", "id", id)
    objectiveRepository.delete(objective)
  }
}
