package uk.gov.justice.digital.hmpps.sentenceplan.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.sentenceplan.model.SentencePlan
import uk.gov.justice.digital.hmpps.sentenceplan.service.SentencePlanService

@RestController
@RequestMapping("/sentence-plan/")
class SentencePlanResource(private val service: SentencePlanService) {

  @PreAuthorize("hasRole('ROLE_SENTENCE_PLAN_RW')")
  @PostMapping
  fun createSentencePlan(@RequestParam crn: String, @RequestBody sentencePlan: SentencePlan): SentencePlan =
    service.createSentencePlan(crn, sentencePlan)
}
