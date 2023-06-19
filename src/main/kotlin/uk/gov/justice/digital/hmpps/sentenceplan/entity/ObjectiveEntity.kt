package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.sentenceplan.exception.NotFoundException
import java.util.Optional
import java.util.UUID

@Entity(name = "Objective")
@Table(name = "objective")
class ObjectiveEntity(
  @ManyToOne
  @JoinColumn(name = "sentence_plan_id", nullable = false)
  val sentencePlan: SentencePlanEntity,

  @Id
  val id: UUID = UUID.randomUUID(),

  var description: String,

  var motivation: String?,

  @OneToMany(mappedBy = "objective")
  val needs: MutableSet<NeedEntity> = mutableSetOf(),
) {

  fun addNeeds(newNeeds: Set<NeedEntity>) {
    needs += newNeeds
  }

  fun removeNeeds(removeNeeds: Set<NeedEntity>) {
    needs -= removeNeeds
  }
}

interface ObjectiveRepository : JpaRepository<ObjectiveEntity, UUID> {
  fun findBySentencePlanId(sentencePlanId: UUID): List<ObjectiveEntity>
  fun findBySentencePlanIdAndId(sentencePlanId: UUID, id: UUID): ObjectiveEntity?

  @EntityGraph(attributePaths = ["needs"])
  override fun findById(id: UUID): Optional<ObjectiveEntity>
}

fun ObjectiveRepository.getBySentencePlanIdAndId(sentencePlanId: UUID, id: UUID) =
  findBySentencePlanIdAndId(sentencePlanId, id) ?: throw NotFoundException("Objective", "id", id)
