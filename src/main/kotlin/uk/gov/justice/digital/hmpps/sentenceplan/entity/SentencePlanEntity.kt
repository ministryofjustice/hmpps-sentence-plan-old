package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime
import java.util.UUID

@Entity(name = "sentencePlan")
@Table(name = "sentence_plan")
class SentencePlanEntity(
  @Id
  val id: UUID,

  @ManyToOne
  @JoinColumn(name = "personId", nullable = false)
  val person: PersonEntity,

  val createdDate: ZonedDateTime,
)

interface SentencePlanRepository : JpaRepository<SentencePlanEntity, UUID> {
  fun getByPersonId(personId: UUID): SentencePlanEntity?
}
