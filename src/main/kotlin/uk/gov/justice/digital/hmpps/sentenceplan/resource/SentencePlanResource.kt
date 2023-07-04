package uk.gov.justice.digital.hmpps.sentenceplan.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.sentenceplan.model.CreateSentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.model.UpdateSentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.service.SentencePlanService
import java.util.UUID

@RestController
@RequestMapping("/sentence-plan")
@PreAuthorize("hasRole('ROLE_SENTENCE_PLAN_RW')")
class SentencePlanResource(private val service: SentencePlanService) {

  @PostMapping
  fun createSentencePlan(@RequestBody sentencePlan: CreateSentencePlan): SentencePlan =
    service.createSentencePlan(sentencePlan)

  @GetMapping
  fun listSentencePlans(@RequestParam crn: String) = service.listSentencePlans(crn)

  @GetMapping("/{id}")
  fun getSentencePlan(@PathVariable id: UUID) = service.findSentencePlan(id)

  @PutMapping("/{id}")
  fun updateSentencePlan(@PathVariable id: UUID, @RequestBody updateSentencePlan: UpdateSentencePlan) =
    service.updateSentencePlan(id, updateSentencePlan)
}
