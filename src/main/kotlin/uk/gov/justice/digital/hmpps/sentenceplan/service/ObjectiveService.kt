package uk.gov.justice.digital.hmpps.sentenceplan.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
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
  private val sentencePlanRepository: SentencePlanRepository,
) {

  /**
   * Create a new objective for a sentence plan.
   *
   */
  fun createObjective(sentencePlanId: UUID, objective: CreateObjective): Objective {
    return sentencePlanRepository.getByIdOrThrow(sentencePlanId)
      .let {
        objectiveRepository.save(ObjectiveEntity(it, description = objective.description)).toModel()
      }
  }

  fun updateObjective(sentencePlanId: UUID, objective: Objective): Objective {
    val original = objectiveRepository.getBySentencePlanIdAndId(sentencePlanId, objective.id)
    val toSave = original.copy(description = objective.description)
    return objectiveRepository.save(toSave).toModel()
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
}
