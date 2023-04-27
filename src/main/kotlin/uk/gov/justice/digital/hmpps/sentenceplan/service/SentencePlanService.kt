package uk.gov.justice.digital.hmpps.sentenceplan.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.exception.ConflictException
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
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
    val person = personRepository.findByCrn(sentencePlanRequest.crn) ?: personRepository.save(PersonEntity(UUID.randomUUID(), sentencePlanRequest.crn))

    val existingSentencePlan = sentencePlanRepository.getByPersonId(person.id)
    when {
      existingSentencePlan != null -> {
        throw ConflictException("Sentence plan already exists for $sentencePlanRequest.crn")
      }

      else -> {
        val sentencePlanSaved =
          sentencePlanRepository.save(SentencePlanEntity(UUID.randomUUID(), person, sentencePlanRequest.createdDate))
        return SentencePlan(sentencePlanSaved.createdDate, sentencePlanSaved.id, person.crn)
      }
    }
  }
}
