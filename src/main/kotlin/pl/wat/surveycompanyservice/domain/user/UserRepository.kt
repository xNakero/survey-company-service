package pl.wat.surveycompanyservice.domain.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CrudRepository<AppUser, Long> {
    fun findByUsername(username: String): AppUser?
}