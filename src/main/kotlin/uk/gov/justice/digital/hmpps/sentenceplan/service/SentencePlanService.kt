package uk.gov.justice.digital.hmpps.sentenceplan.service

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.exception.ConflictException
import uk.gov.justice.digital.hmpps.sentenceplan.exception.NotFoundException
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateSentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlanList
import uk.gov.justice.digital.hmpps.sentenceplan.model.UpdateSentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.toModel
import java.time.ZonedDateTime
import java.util.UUID

@Service
class SentencePlanService(
  private val personRepository: PersonRepository,
  private val sentencePlanRepository: SentencePlanRepository,
  private val objectiveRepository: ObjectiveRepository,
) {

  /**
   * Create a new sentence plan for a crn.
   *
   */
  fun createSentencePlan(sentencePlanRequest: CreateSentencePlan): SentencePlan {
    val person = personRepository.findByCrn(sentencePlanRequest.crn)
      ?: personRepository.save(PersonEntity(sentencePlanRequest.crn))

    return when {
      sentencePlanRepository.existsByPersonIdAndClosedDateIsNull(person.id) ->
        throw ConflictException("Sentence plan already exists for $sentencePlanRequest.crn")

      else -> sentencePlanRepository.save(SentencePlanEntity(person, ZonedDateTime.now())).toModel()
    }
  }

  fun updateSentencePlan(id: UUID, updateSentencePlan: UpdateSentencePlan): SentencePlan {
    val sentencePlanEntity = findSentencePlanEntity(id)
    sentencePlanEntity.riskFactors = updateSentencePlan.riskFactors
    sentencePlanEntity.protectiveFactors = updateSentencePlan.protectiveFactors
    sentencePlanEntity.practitionerComments = updateSentencePlan.practitionerComments
    sentencePlanEntity.individualComments = updateSentencePlan.individualComments
    return sentencePlanRepository.save(sentencePlanEntity).toModel()
  }

  /**
   * Get all existing sentence plans for a crn.
   *
   */
  fun listSentencePlans(crn: String) = SentencePlanList(
    sentencePlans = personRepository.findByCrn(crn)
      ?.let { person -> sentencePlanRepository.findByPersonId(person.id) }
      ?.map { it.toModel() }
      ?: emptyList(),
  )

  fun findSentencePlan(id: UUID): SentencePlan = sentencePlanRepository.findByIdOrNull(id)?.toModel()
    ?: throw NotFoundException("SentencePlan", "id", id)

  fun findSentencePlanEntity(id: UUID): SentencePlanEntity = sentencePlanRepository.findByIdOrNull(id)
    ?: throw NotFoundException("SentencePlan", "id", id)

  @Transactional
  fun deleteSentencePlan(id: UUID) {
    val sentencePlan = sentencePlanRepository.findByIdOrNull(id)
      ?: throw NotFoundException("SentencePlan", "id", id)
    objectiveRepository.deleteAllBySentencePlan(sentencePlan)
    sentencePlanRepository.delete(sentencePlan)
  }
}
