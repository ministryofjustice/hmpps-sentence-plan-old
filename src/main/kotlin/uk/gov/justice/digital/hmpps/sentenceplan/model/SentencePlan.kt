package uk.gov.justice.digital.hmpps.sentenceplan.model

import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanEntity
import java.time.ZonedDateTime
import java.util.UUID

data class SentencePlan(
  val id: UUID,
  val crn: String,
  val createdDate: ZonedDateTime = ZonedDateTime.now(),
  val activeDate: ZonedDateTime? = null,
  val closedDate: ZonedDateTime? = null,
  val status: String = when {
    closedDate != null && closedDate.isBefore(ZonedDateTime.now()) -> "Closed"
    activeDate != null && activeDate.isBefore(ZonedDateTime.now()) -> "Active"
    else -> "Draft"
  },
  val riskFactors: String? = null,
  val protectiveFactors: String? = null,
  val practitionerComments: String? = null,
  val individualComments: String? = null,
)

data class CreateSentencePlan(
  val crn: String,
)

data class UpdateSentencePlan(
  val riskFactors: String? = null,
  val protectiveFactors: String? = null,
  val practitionerComments: String? = null,
  val individualComments: String? = null,
)

fun SentencePlanEntity.toModel() = SentencePlan(
  id = id,
  crn = person.crn,
  createdDate = createdDate,
  activeDate = activeDate,
  closedDate = closedDate,
  riskFactors = riskFactors,
  protectiveFactors = protectiveFactors,
  practitionerComments = practitionerComments,
  individualComments = individualComments,
)
