package uk.gov.justice.digital.hmpps.sentenceplan.exception

class NotActiveException(entity: String, field: String, value: Any) :
  RuntimeException("$entity with $field of $value not active")
