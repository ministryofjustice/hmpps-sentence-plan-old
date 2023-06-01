package uk.gov.justice.digital.hmpps.sentenceplan.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateObjective
import uk.gov.justice.digital.hmpps.sentenceplan.model.Objective
import uk.gov.justice.digital.hmpps.sentenceplan.service.ObjectiveService
import java.util.UUID

@RestController
@RequestMapping("/sentence-plan/{sentencePlanId}/objective")
@PreAuthorize("hasRole('ROLE_SENTENCE_PLAN_RW')")
class ObjectiveResource(private val service: ObjectiveService) {

  @PostMapping
  fun createObjective(@PathVariable sentencePlanId: UUID, @RequestBody objective: CreateObjective): Objective =
    service.createObjective(sentencePlanId, objective)

  @GetMapping
  fun listSentencePlans(@PathVariable sentencePlanId: UUID) = service.listObjectives(sentencePlanId)

  @GetMapping("/{id}")
  fun getSentencePlanObjective(@PathVariable sentencePlanId: UUID, @PathVariable id: UUID) = service.findObjective(id)
}
