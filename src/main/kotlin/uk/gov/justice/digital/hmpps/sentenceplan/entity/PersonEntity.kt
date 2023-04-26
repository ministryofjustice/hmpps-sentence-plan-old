package uk.gov.justice.digital.hmpps.sentenceplan.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.sentenceplan.exception.NotFoundException
import java.util.UUID

@Entity(name = "person")
@Table(name = "person")
class PersonEntity(
  @Id
  val id: UUID,
  val crn: String,
)

interface PersonRepository : JpaRepository<PersonEntity, UUID> {
  fun findByCrn(crn: String): PersonEntity?
}
fun PersonRepository.getByCrn(crn: String) =
  findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
