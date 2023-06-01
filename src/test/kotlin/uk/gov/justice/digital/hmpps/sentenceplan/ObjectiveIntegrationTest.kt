package uk.gov.justice.digital.hmpps.sentenceplan

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.json
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.getByCrn
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateObjective
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateSentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.Objective
import uk.gov.justice.digital.hmpps.sentenceplan.model.ObjectiveList
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import java.util.UUID

@AutoConfigureMockMvc
@ActiveProfiles("test")
@WireMockTest
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ObjectiveIntegrationTest {
  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var sentencePlanRepository: SentencePlanRepository

  @Autowired
  lateinit var objectiveRepository: ObjectiveRepository

  @Autowired
  lateinit var personRepository: PersonRepository

  @BeforeEach
  fun cleanup() {
    objectiveRepository.deleteAll()
    sentencePlanRepository.deleteAll()
  }

  @Test
  fun `create an objective`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123321Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjective(sentencePlans[0].id, wireMockRuntimeInfo)

    val objectiveRetrieved = objectiveRepository.findById(objectiveSaved.id)

    assertThat(objectiveRetrieved.get().description).isEqualTo(objectiveSaved.description)
  }

  @Test
  fun `get an objective`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123321Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjective(sentencePlans[0].id, wireMockRuntimeInfo)
    val objectiveRetrieved = objectMapper.readValue<Objective>(
      mockMvc.perform(get("/sentence-plan/${sentencePlans[0].id}/objective/${objectiveSaved.id}").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl))
        .andExpect(status().is2xxSuccessful)
        .andReturn()
        .response.contentAsString,
    )

    assertThat(objectiveRetrieved.description).isEqualTo(objectiveSaved.description)
  }

  @Test
  fun `get all objectives`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123321Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjective(sentencePlans[0].id, wireMockRuntimeInfo)
    val objectiveRetrieved = objectMapper.readValue<ObjectiveList>(
      mockMvc.perform(get("/sentence-plan/${sentencePlans[0].id}/objective").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl))
        .andExpect(status().is2xxSuccessful)
        .andReturn()
        .response.contentAsString,
    )
    assertThat(objectiveRetrieved.objectives.size).isEqualTo(1)
    assertThat(objectiveRetrieved.objectives[0].description).isEqualTo(objectiveSaved.description)
  }

  private fun createSentencePlan(
    crn: String,
    wireMockRuntimeInfo: WireMockRuntimeInfo,
    json: String = objectMapper.writeValueAsString(CreateSentencePlan(crn)),
  ) = objectMapper.readValue<SentencePlan>(
    mockMvc.perform(post("/sentence-plan").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl).json(json))
      .andExpect(status().is2xxSuccessful)
      .andReturn()
      .response.contentAsString,
  )

  private fun createObjective(
    sentencePlanId: UUID,
    wireMockRuntimeInfo: WireMockRuntimeInfo,
    json: String = objectMapper.writeValueAsString(CreateObjective("objective for sp: $sentencePlanId")),
  ) = objectMapper.readValue<Objective>(
    mockMvc.perform(post("/sentence-plan/$sentencePlanId/objective").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl).json(json))
      .andExpect(status().is2xxSuccessful)
      .andReturn()
      .response.contentAsString,
  )
}
