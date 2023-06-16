package uk.gov.justice.digital.hmpps.sentenceplan

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.security.json
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ActionRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.NeedRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.ObjectiveRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.PersonRepository
import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanRepository
import uk.gov.justice.digital.hmpps.sentenceplan.model.Action
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateAction
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateObjective
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateSentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.Need
import uk.gov.justice.digital.hmpps.sentenceplan.model.Objective
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import java.util.UUID

@AutoConfigureMockMvc
@ActiveProfiles("test")
@WireMockTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActionIntegrationTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var objectMapper: ObjectMapper

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
  fun `createAction should create a new action`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val sentencePlanId = createSentencePlan("C123123X", wireMockRuntimeInfo).id
    val objectiveId = createObjective(sentencePlanId, wireMockRuntimeInfo).id
    val createAction = CreateAction(
      "Test Action",
      true,
      "INT123",
      "local",
      "TODO",
      individualOwner = true,
      practitionerOwner = false,
      otherOwner = "Social worker",
    )

    mockMvc.perform(
      post("/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action", sentencePlanId, objectiveId)
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createAction)),
    )
      .andExpect(status().isCreated)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.description").value(createAction.description))
      .andExpect(jsonPath("$.status").value(createAction.status))
      .andExpect(jsonPath("$.interventionParticipation").value(createAction.interventionParticipation))
      .andExpect(jsonPath("$.interventionName").value(createAction.interventionName))
      .andExpect(jsonPath("$.interventionType").value(createAction.interventionType))
      .andExpect(jsonPath("$.otherOwner").value(createAction.otherOwner))
      .andExpect(jsonPath("$.individualOwner").value(createAction.individualOwner))
      .andExpect(jsonPath("$.practitionerOwner").value(createAction.practitionerOwner))

    Assertions.assertEquals(actionRepository.findAllByObjectiveIdOrderByCreatedDateTimeAsc(objectiveId).size, 1)
  }

  @Test
  fun `get a single action`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val sentencePlanId = createSentencePlan("C123123X", wireMockRuntimeInfo).id
    val objectiveId = createObjective(sentencePlanId, wireMockRuntimeInfo).id
    val createAction = CreateAction(
      "Test Action",
      true,
      "INT123",
      "local",
      "TODO",
      individualOwner = true,
      practitionerOwner = false,
      otherOwner = "Social worker",
    )

    val actionRetrieved = objectMapper.readValue<Action>(
      mockMvc.perform(
        post("/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action", sentencePlanId, objectiveId)
          .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createAction)),
      )
        .andReturn()
        .response.contentAsString,
    )

    mockMvc.perform(
      get(
        "/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action/{actionId}",
        sentencePlanId,
        objectiveId,
        actionRetrieved.id,
      )
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl),
    ).andExpect(status().is2xxSuccessful)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.description").value(createAction.description))
      .andExpect(jsonPath("$.status").value(createAction.status))
      .andExpect(jsonPath("$.interventionParticipation").value(createAction.interventionParticipation))
      .andExpect(jsonPath("$.interventionName").value(createAction.interventionName))
      .andExpect(jsonPath("$.interventionType").value(createAction.interventionType))
      .andExpect(jsonPath("$.otherOwner").value(createAction.otherOwner))
      .andExpect(jsonPath("$.individualOwner").value(createAction.individualOwner))
      .andExpect(jsonPath("$.practitionerOwner").value(createAction.practitionerOwner))
  }

  @Test
  fun `get a list of actions`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val sentencePlanId = createSentencePlan("C123123X", wireMockRuntimeInfo).id
    val objectiveId = createObjective(sentencePlanId, wireMockRuntimeInfo).id
    val createAction = CreateAction(
      "Test Action 1",
      true,
      "INT123",
      "local",
      "TODO",
      individualOwner = true,
      practitionerOwner = false,
      otherOwner = "Social worker",
    )

    val actionRetrieved = objectMapper.readValue<Action>(
      mockMvc.perform(
        post("/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action", sentencePlanId, objectiveId)
          .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createAction)),
      )
        .andReturn()
        .response.contentAsString,
    )

    val createActionTwo = CreateAction(
      "Test Action 2",
      true,
      "INT123",
      "local",
      "TODO",
      individualOwner = true,
      practitionerOwner = false,
      otherOwner = "Social worker",
    )

    val actionRetrievedTwo = objectMapper.readValue<Action>(
      mockMvc.perform(
        post("/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action", sentencePlanId, objectiveId)
          .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createActionTwo)),
      )
        .andReturn()
        .response.contentAsString,
    )

    mockMvc.perform(
      get(
        "/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action",
        sentencePlanId,
        objectiveId,
      )
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl),
    ).andExpect(status().is2xxSuccessful)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.actions[0].description").value(createAction.description))
      .andExpect(jsonPath("$.actions[0].status").value(createAction.status))
      .andExpect(jsonPath("$.actions[0].interventionParticipation").value(createAction.interventionParticipation))
      .andExpect(jsonPath("$.actions[0].interventionName").value(createAction.interventionName))
      .andExpect(jsonPath("$.actions[0].interventionType").value(createAction.interventionType))
      .andExpect(jsonPath("$.actions[0].otherOwner").value(createAction.otherOwner))
      .andExpect(jsonPath("$.actions[0].individualOwner").value(createAction.individualOwner))
      .andExpect(jsonPath("$.actions[0].practitionerOwner").value(createAction.practitionerOwner))
      .andExpect(jsonPath("$.actions[1].description").value(createActionTwo.description))
  }

  @Test
  fun `delete should delete an action`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val sentencePlanId = createSentencePlan("C123123X", wireMockRuntimeInfo).id
    val objectiveId = createObjective(sentencePlanId, wireMockRuntimeInfo).id
    val createAction = CreateAction(
      "Test Action",
      true,
      "INT123",
      "local",
      "TODO",
      individualOwner = true,
      practitionerOwner = false,
      otherOwner = "Social worker",
    )

    val actionRetrieved = objectMapper.readValue<Action>(
      mockMvc.perform(
        post("/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action", sentencePlanId, objectiveId)
          .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createAction)),
      )
        .andReturn()
        .response.contentAsString,
    )

    mockMvc.perform(
      delete(
        "/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action/{actionId}",
        sentencePlanId,
        objectiveId,
        actionRetrieved.id,
      )
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl),
    )

    Assertions.assertTrue(actionRepository.findById(actionRetrieved.id).isEmpty)
  }

  @Test
  fun `update an action`(wireMockRuntimeInfo: WireMockRuntimeInfo) {
    val sentencePlanId = createSentencePlan("C123123X", wireMockRuntimeInfo).id
    val objectiveId = createObjective(sentencePlanId, wireMockRuntimeInfo).id
    val createAction = CreateAction(
      "Test Action",
      true,
      "INT123",
      "local",
      "TODO",
      individualOwner = true,
      practitionerOwner = false,
      otherOwner = "Social worker",
    )

    val actionRetrieved = objectMapper.readValue<Action>(
      mockMvc.perform(
        post("/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action", sentencePlanId, objectiveId)
          .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(createAction)),
      )
        .andReturn()
        .response.contentAsString,
    )

    val actionRetrievedCopy = actionRetrieved.copy(description = "new Description")
    val json: String = objectMapper.writeValueAsString(actionRetrievedCopy)
    mockMvc.perform(
      put(
        "/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action/{actionId}",
        sentencePlanId,
        objectiveId,
        actionRetrieved.id,
      )
        .withOAuth2Token(wireMockRuntimeInfo.httpBaseUrl)
        .json(json),
    )

    Assertions.assertEquals(
      actionRepository.findById(actionRetrieved.id).get().description,
      actionRetrievedCopy.description,
    )
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
}
