package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class NumericToBooleanConverter : AttributeConverter<Boolean, Int> {

  override fun convertToDatabaseColumn(attribute: Boolean): Int {
    return if (attribute) 1 else 0
  }

  override fun convertToEntityAttribute(dbData: Int): Boolean {
    return dbData != 0
  }
}
