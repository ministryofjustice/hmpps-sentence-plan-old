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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.json
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.NeedRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanEntity
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.getByCrn
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateSentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlanEngagement
import java.time.ZonedDateTime
import java.util.UUID

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
  lateinit var objectiveRepository: ObjectiveRepository

  @Autowired
  lateinit var personRepository: PersonRepository

  @Autowired
  lateinit var needRepository: NeedRepository

  @Autowired
  lateinit var actionRepository: ActionRepository

  @BeforeEach
  fun cleanup() {
    actionRepository.deleteAll()
    needRepository.deleteAll()
    objectiveRepository.deleteAll()
    sentencePlanRepository.deleteAll()
  }

  @Test
  fun `create a sentence plan`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123321Z"

    createSentencePlan(crn, wireMockRuntimeInfo)

    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    assertThat(sentencePlans).hasSize(1)
  }

  @Test
  fun `update engagement details`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123321Z"

    val sentencePlan = createSentencePlan(crn, wireMockRuntimeInfo)

    updateEngagementDetails(sentencePlan.id, wireMockRuntimeInfo)

    val updatedSentencePlan = sentencePlanRepository.findById(sentencePlan.id).orElseThrow()
    assertThat(updatedSentencePlan.riskFactors).isEqualTo("some risk text")
    assertThat(updatedSentencePlan.protectiveFactors).isEqualTo("some protective text")
  }

  @Test
  fun `sentence plan already exists`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123321Z"
    val content = CreateSentencePlan(crn)

    createSentencePlan(crn, wireMockRuntimeInfo)

    mockMvc.perform(
      post("/sentence-plan")
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
        .json(objectMapper.writeValueAsString(content)),
    )
      .andExpect(status().isConflict)
  }

  @Test
  fun `list all sentence plans for a case`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123321Z"

    repeat(3) {
      createSentencePlan(crn, wireMockRuntimeInfo)
      sentencePlanRepository.saveAll(
        sentencePlanRepository.findAll().map { it.withClosedDate(closedDate = ZonedDateTime.now()) },
      )
    }

    mockMvc.perform(get("/sentence-plan?crn=$crn").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.sentencePlans").isNotEmpty)
      .andExpect(jsonPath("$.sentencePlans.size()").value(3))
  }

  @Test
  fun `can retrieve a single sentence plan by id`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "S123456"

    val sentencePlan = createSentencePlan(crn, wireMockRuntimeInfo)

    mockMvc.perform(
      get("/sentence-plan/${sentencePlan.id}")
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl),
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.id").value(sentencePlan.id.toString()))
      .andExpect(jsonPath("$.crn").value(crn))
  }

  fun SentencePlanEntity.withClosedDate(
    closedDate: ZonedDateTime?,
  ): SentencePlanEntity = SentencePlanEntity(person, createdDate, activeDate, closedDate, id = id)

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

  private fun updateEngagementDetails(
    id: UUID,
    wireMockRuntimeInfo: WireMockRuntimeInfo,
    json: String = objectMapper.writeValueAsString(SentencePlanEngagement("some risk text", "some protective text")),
  ) = objectMapper.readValue<SentencePlan>(
    mockMvc.perform(put("/sentence-plan/$id").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl).json(json))
      .andExpect(status().is2xxSuccessful)
      .andReturn()
      .response.contentAsString,
  )
}
