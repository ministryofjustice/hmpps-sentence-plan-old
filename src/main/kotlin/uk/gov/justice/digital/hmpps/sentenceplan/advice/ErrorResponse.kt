package uk.gov.justice.digital.hmpps.sentenceplan.advice

data class ErrorResponse(
  val status: Int,
  val message: String? = null,
  val fields: List<FieldError>? = null,
)

data class FieldError(
  val type: String?,
  val message: String?,
  val field: String?,
)
