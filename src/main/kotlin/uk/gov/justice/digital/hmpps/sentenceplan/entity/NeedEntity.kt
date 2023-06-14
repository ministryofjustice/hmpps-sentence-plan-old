package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

@Entity(name = "Need")
@Table(name = "need")
class NeedEntity(
  val code: String,

  @ManyToOne
  @JoinColumn(name = "objective_id", nullable = false)
  val objective: ObjectiveEntity,

  @Id
  val id: UUID = UUID.randomUUID(),
)

interface NeedRepository : JpaRepository<NeedEntity, UUID>
