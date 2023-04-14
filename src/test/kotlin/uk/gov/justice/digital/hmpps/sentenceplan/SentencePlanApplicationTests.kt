package uk.gov.justice.digital.hmpps.sentenceplan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.sentenceplan.person.PersonEntity
import uk.gov.justice.digital.hmpps.sentenceplan.person.PersonRepository
import java.util.UUID

@SpringBootTest
internal class SentencePlanApplicationTests {
  @Autowired
  internal lateinit var personRepository: PersonRepository

  @Test
  fun `can create and read a person`() {
    val person = PersonEntity(UUID.randomUUID(), "X12312B", "LM1234ML")
    personRepository.save(person)

    val saved = personRepository.findById(person.id).orElseThrow()
    assertThat(saved.crn).isEqualTo(person.crn)
    assertThat(saved.nomsId).isEqualTo(person.nomsId)
  }
}
