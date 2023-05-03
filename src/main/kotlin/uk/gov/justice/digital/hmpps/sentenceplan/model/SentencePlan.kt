package uk.gov.justice.digital.hmpps.sentenceplan.model

import uk.gov.justice.digital.hmpps.sentenceplan.entity.SentencePlanEntity
import java.time.ZonedDateTime
import java.util.UUID

data class SentencePlan(
  val id: UUID?,
  val crn: String,
  val createdDate: ZonedDateTime = ZonedDateTime.now(),
  val activeDate: ZonedDateTime? = null,
  val closedDate: ZonedDateTime? = null,
  val status: String = when {
    closedDate != null && closedDate.isBefore(ZonedDateTime.now()) -> "Closed"
    activeDate != null && activeDate.isBefore(ZonedDateTime.now()) -> "Active"
    else -> "Draft"
  },
)

fun SentencePlanEntity.toModel() = SentencePlan(
  id = id,
  crn = person.crn,
  createdDate = createdDate,
  activeDate = activeDate,
  closedDate = closedDate,
)
