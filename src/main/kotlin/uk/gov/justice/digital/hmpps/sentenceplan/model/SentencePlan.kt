package uk.gov.justice.digital.hmpps.sentenceplan.model

import java.time.ZonedDateTime
import java.util.UUID

data class SentencePlan(
  val createdDate: ZonedDateTime,
  val id: UUID?,
  val crn: String,
)
