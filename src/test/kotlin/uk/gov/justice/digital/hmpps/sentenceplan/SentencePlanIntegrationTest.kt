package uk.gov.justice.digital.hmpps.sentenceplan

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.getByCrn
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@ActiveProfiles("test")
@WireMockTest
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SentencePlanIntegrationTest {
  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var sentencePlanRepository: SentencePlanRepository

  @Autowired
  lateinit var personRepository: PersonRepository

  @Test
  fun `successful response`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val content = SentencePlan(createdDate = ZonedDateTime.now(), null)

    val crn = "X123321Z"

    mockMvc.perform(
      post("/offenders/$crn/sentence-plan")
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(content)),
    )
      .andExpect(status().is2xxSuccessful)

    val sentencePlan = sentencePlanRepository.getByPersonId(personRepository.getByCrn(crn).id)
    assertThat(sentencePlan).isNotNull
    sentencePlanRepository.delete(sentencePlan!!)
  }

  @Test
  fun `sentence plan already exists`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val content = SentencePlan(createdDate = ZonedDateTime.now(), null)

    val crn = "X123321Z"

    mockMvc.perform(
      post("/offenders/$crn/sentence-plan")
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(content)),
    )
      .andExpect(status().is2xxSuccessful)

    mockMvc.perform(
      post("/offenders/$crn/sentence-plan")
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(content)),
    )
      .andExpect(status().isConflict)

    val sentencePlan = sentencePlanRepository.getByPersonId(personRepository.getByCrn(crn).id)
    sentencePlanRepository.delete(sentencePlan!!)
  }
}
