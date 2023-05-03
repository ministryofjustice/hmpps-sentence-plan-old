package uk.gov.justice.digital.hmpps.sentenceplan.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.exception.ConflictException
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlanList
import uk.gov.justice.digital.hmpps.sentenceplan.model.toModel
import java.time.ZonedDateTime
import java.util.UUID

@Service
class SentencePlanService(
  private val personRepository: PersonRepository,
  private val sentencePlanRepository: SentencePlanRepository,
) {

  /**
   * Create a new sentence plan for a crn.
   *
   */
  fun createSentencePlan(sentencePlanRequest: SentencePlan): SentencePlan {
    val person = personRepository.findByCrn(sentencePlanRequest.crn)
      ?: personRepository.save(PersonEntity(UUID.randomUUID(), sentencePlanRequest.crn))

    return when {
      sentencePlanRepository.existsByPersonIdAndClosedDateIsNull(person.id) ->
        throw ConflictException("Sentence plan already exists for $sentencePlanRequest.crn")

      else -> sentencePlanRepository.save(SentencePlanEntity(UUID.randomUUID(), person, ZonedDateTime.now())).toModel()
    }
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
}
