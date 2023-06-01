package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

@Entity(name = "Objective")
@Table(name = "objective")
class ObjectiveEntity(
  @ManyToOne
  @JoinColumn(name = "sentence_plan_id", nullable = false)
  val sentencePlan: SentencePlanEntity,

  @Id
  val id: UUID = UUID.randomUUID(),

  val description: String,
)

interface ObjectiveRepository : JpaRepository<ObjectiveEntity, UUID> {
  fun findBySentencePlanId(sentencePlanId: UUID): List<ObjectiveEntity>
}
