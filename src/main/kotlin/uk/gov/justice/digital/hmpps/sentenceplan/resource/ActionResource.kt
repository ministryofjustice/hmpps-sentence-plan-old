package uk.gov.justice.digital.hmpps.sentenceplan.resource

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.sentenceplan.model.Action
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateAction
import uk.gov.justice.digital.hmpps.sentenceplan.service.ActionService
import java.util.UUID

@RestController
@RequestMapping("/sentence-plan/{sentencePlanId}/objective/{objectiveId}/action")
@PreAuthorize("hasRole('ROLE_SENTENCE_PLAN_RW')")
class ActionResource(private val service: ActionService) {

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createAction(
    @PathVariable sentencePlanId: UUID,
    @PathVariable objectiveId: UUID,
    @RequestBody action: CreateAction,
  ): Action = service.createAction(sentencePlanId, objectiveId, action)

  @PutMapping("/{id}")
  fun updateAction(
    @PathVariable sentencePlanId: UUID,
    @PathVariable objectiveId: UUID,
    @PathVariable id: UUID,
    @RequestBody action: Action,
  ): Action = service.updateAction(sentencePlanId, objectiveId, id, action)

  @DeleteMapping("/{id}")
  fun deleteAction(
    @PathVariable sentencePlanId: UUID,
    @PathVariable objectiveId: UUID,
    @PathVariable id: UUID,
  ) {
    service.deleteAction(sentencePlanId, objectiveId, id)
  }

  @GetMapping
  fun listActions(@PathVariable sentencePlanId: UUID, @PathVariable objectiveId: UUID) =
    service.listActions(sentencePlanId, objectiveId)

  @GetMapping("/{id}")
  fun getAction(@PathVariable sentencePlanId: UUID, @PathVariable objectiveId: UUID, @PathVariable id: UUID) =
    service.findAction(sentencePlanId, objectiveId, id)
}
