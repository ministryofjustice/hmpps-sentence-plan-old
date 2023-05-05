package uk.gov.justice.digital.hmpps.sentenceplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
internal class SentencePlanApplicationTests {
  @Autowired
  internal lateinit var personRepository: PersonRepository

  @Autowired
  internal lateinit var sentencePlanRepository: SentencePlanRepository

  @Test
  fun `can create and read a person`() {
    val person = PersonEntity("X12312B")
    personRepository.save(person)

    val saved = personRepository.findById(person.id).orElseThrow()
    assertThat(saved.crn).isEqualTo(person.crn)

    personRepository.delete(saved)
  }

  @Test
  fun `can create and read a sentence plan`() {
    val person = PersonEntity("X12312E")
    personRepository.save(person)

    val saved = personRepository.findById(person.id).orElseThrow()
    val createdDate = ZonedDateTime.now()
    val sentencePlan = SentencePlanEntity(UUID.randomUUID(), person, createdDate)
    sentencePlanRepository.save(sentencePlan)
    val sentencePlanSaved = sentencePlanRepository.findByPersonId(person.id)

    assertThat(sentencePlanSaved).hasSize(1)
    assertThat(sentencePlanSaved[0].createdDate.truncatedTo(ChronoUnit.SECONDS))
      .isEqualTo(createdDate.truncatedTo(ChronoUnit.SECONDS))
    sentencePlanRepository.delete(sentencePlanSaved[0])

    personRepository.delete(saved)
  }
}
