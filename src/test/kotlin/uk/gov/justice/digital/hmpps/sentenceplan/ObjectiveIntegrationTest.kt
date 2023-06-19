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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.json
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.NeedRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.getByCrn
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateObjective
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateSentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.Need
import uk.gov.justice.digital.hmpps.sentenceplan.model.Objective
import uk.gov.justice.digital.hmpps.sentenceplan.model.ObjectiveList
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.toModel
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

  @Autowired
  lateinit var actionRepository: ActionRepository

  @Autowired
  lateinit var needRepository: NeedRepository

  @BeforeEach
  fun cleanup() {
    actionRepository.deleteAll()
    needRepository.deleteAll()
    objectiveRepository.deleteAll()
    sentencePlanRepository.deleteAll()
  }

  @Test
  fun `create an objective`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123322Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjective(sentencePlans[0].id, wireMockRuntimeInfo)

    val objectiveRetrieved = objectiveRepository.findById(objectiveSaved.id)

    assertThat(objectiveRetrieved.get().description).isEqualTo(objectiveSaved.description)
  }

  @Test
  fun `create an objective without need`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123322Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjectiveWithoutNeed(sentencePlans[0].id, wireMockRuntimeInfo)

    val objectiveRetrieved = objectiveRepository.findById(objectiveSaved.id)

    assertThat(objectiveRetrieved.get().description).isEqualTo(objectiveSaved.description)
  }

  @Test
  fun `update an objective change description`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123322Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjective(sentencePlans[0].id, wireMockRuntimeInfo)
    val objectiveRetrieved = objectiveRepository.findById(objectiveSaved.id).orElseThrow()
    objectiveRetrieved.description = "updated ${objectiveRetrieved.description}"
    updateObjective(objectiveRetrieved.sentencePlan.id, objectiveRetrieved.toModel(), wireMockRuntimeInfo)

    val updatedObjectiveRetrieved = objectiveRepository.findById(objectiveSaved.id).orElseThrow()

    assertThat(updatedObjectiveRetrieved.description).isEqualTo(objectiveRetrieved.description)
  }

  @Test
  fun `update an objective add need`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123322Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjective(sentencePlans[0].id, wireMockRuntimeInfo)
    val objectiveRetrieved = objectiveRepository.findById(objectiveSaved.id).orElseThrow()
    val objectiveModel = objectiveRetrieved.toModel()
    updateObjective(
      objectiveRetrieved.sentencePlan.id,
      objectiveModel.copy(needs = objectiveModel.needs + Need("Education")),
      wireMockRuntimeInfo,
    )

    val updatedObjectiveRetrieved = objectiveRepository.findById(objectiveSaved.id).orElseThrow()

    assertThat(updatedObjectiveRetrieved.description).isEqualTo(objectiveRetrieved.description)
    assertThat(updatedObjectiveRetrieved.needs.size).isEqualTo(2)
  }

  @Test
  fun `update an objective delete need`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123322Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjective(sentencePlans[0].id, wireMockRuntimeInfo)
    val objectiveRetrieved = objectiveRepository.findById(objectiveSaved.id).orElseThrow()
    val objectiveModel = objectiveRetrieved.toModel()
    updateObjective(
      objectiveRetrieved.sentencePlan.id,
      objectiveModel.copy(needs = setOf(Need("Education"))),
      wireMockRuntimeInfo,
    )

    val updatedObjectiveRetrieved = objectiveRepository.findById(objectiveSaved.id).orElseThrow()

    assertThat(updatedObjectiveRetrieved.description).isEqualTo(objectiveRetrieved.description)
    assertThat(updatedObjectiveRetrieved.motivation).isEqualTo(objectiveRetrieved.motivation)
    assertThat(updatedObjectiveRetrieved.needs.size).isEqualTo(1)
  }

  @Test
  fun `get an objective`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123322Z"

    createSentencePlan(crn, wireMockRuntimeInfo)
    val sentencePlans = sentencePlanRepository.findByPersonId(personRepository.getByCrn(crn).id)
    val objectiveSaved = createObjective(sentencePlans[0].id, wireMockRuntimeInfo)
    val objectiveRetrieved = objectMapper.readValue<Objective>(
      mockMvc.perform(
        get("/sentence-plan/${sentencePlans[0].id}/objective/${objectiveSaved.id}").withOAuth2Token(
          wireMockRuntimeInfo.httpBaseUrl,
        ),
      )
        .andExpect(status().is2xxSuccessful)
        .andReturn()
        .response.contentAsString,
    )

    assertThat(objectiveRetrieved.description).isEqualTo(objectiveSaved.description)
  }

  @Test
  fun `get all objectives`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val crn = "X123322Z"

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
    json: String = objectMapper.writeValueAsString(
      CreateObjective(
        "objective for sp: $sentencePlanId",
        "Contemplation",
        setOf(Need("relationships")),
      ),
    ),
  ) = objectMapper.readValue<Objective>(
    mockMvc.perform(
      post("/sentence-plan/$sentencePlanId/objective").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl).json(json),
    )
      .andExpect(status().is2xxSuccessful)
      .andReturn()
      .response.contentAsString,
  )

  private fun createObjectiveWithoutNeed(
    sentencePlanId: UUID,
    wireMockRuntimeInfo: WireMockRuntimeInfo,
    json: String = objectMapper.writeValueAsString(
      CreateObjective(
        "objective for sp: $sentencePlanId",
        "Contemplation",
      ),
    ),
  ) = objectMapper.readValue<Objective>(
    mockMvc.perform(
      post("/sentence-plan/$sentencePlanId/objective").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl).json(json),
    )
      .andExpect(status().is2xxSuccessful)
      .andReturn()
      .response.contentAsString,
  )

  private fun updateObjective(
    sentencePlanId: UUID,
    objective: Objective,
    wireMockRuntimeInfo: WireMockRuntimeInfo,
    json: String = objectMapper.writeValueAsString(objective),
  ) = objectMapper.readValue<Objective>(
    mockMvc.perform(
      put("/sentence-plan/$sentencePlanId/objective/${objective.id}").withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
        .json(json),
    )
      .andExpect(status().is2xxSuccessful)
      .andReturn()
      .response.contentAsString,
  )
}
