package uk.gov.justice.digital.hmpps.sentenceplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import java.time.ZonedDateTime
import java.util.UUID

@SpringBootTest
internal class SentencePlanApplicationTests {
  @Autowired
  internal lateinit var personRepository: PersonRepository
  @Autowired
  internal lateinit var sentencePlanRepository: SentencePlanRepository

  @Test
  fun `can create and read a person`() {
    val person = PersonEntity(UUID.randomUUID(), "X12312B", "LM1234ML")
    personRepository.save(person)

    val saved = personRepository.findById(person.id).orElseThrow()
    assertThat(saved.crn).isEqualTo(person.crn)
    assertThat(saved.nomsId).isEqualTo(person.nomsId)

    personRepository.delete(saved)
  }

  @Test
  fun `can create and read a sentence plan`() {
    val person = PersonEntity(UUID.randomUUID(), "X12312D", "LM1234MZ")
    personRepository.save(person)

    val saved = personRepository.findById(person.id).orElseThrow()
    val createdDate = ZonedDateTime.now()
    val sentencePlan = SentencePlanEntity(UUID.randomUUID(), person, createdDate)
    sentencePlanRepository.save(sentencePlan)
    val sentencePlanSaved = sentencePlanRepository.getByPersonId(person.id)

    if(sentencePlanSaved!=null) {
      assertThat(sentencePlanSaved.createdDate).isEqualTo(createdDate)
      sentencePlanRepository.delete(sentencePlanSaved)
    }
    personRepository.delete(saved)
  }
}
