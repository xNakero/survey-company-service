package pl.wat.surveycompanyservice.domain.user

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import pl.wat.surveycompanyservice.domain.role.AppRole
import pl.wat.surveycompanyservice.domain.role.RoleRepository
import java.lang.RuntimeException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager
) {

    fun login(username: String, password: String): Authentication {
        val usernamePasswordAuth = UsernamePasswordAuthenticationToken(username, password)
        val authentication = authenticationManager.authenticate(usernamePasswordAuth)
        SecurityContextHolder.getContext().authentication = authentication
        return authentication
    }

    fun createUser(username: String, password: String, role: AppRole) {
        val existingRole = roleRepository.findByName(role.toString()) ?: throw RoleNotFoundException("There is no role $role.")
        userRepository.findByUsername(username)?.let { throw UserAlreadyExistsException("User with username=$username already exists.") }
        val user = userRepository.save(
            AppUser(
                username = username,
                password = passwordEncoder.encode(password),
                roles = mutableSetOf(existingRole)
            ))
        existingRole.users.add(user)
        roleRepository.save(existingRole)
    }

}

class UserAlreadyExistsException(message: String?) : RuntimeException(message)
class RoleNotFoundException(message: String?): RuntimeException(message)