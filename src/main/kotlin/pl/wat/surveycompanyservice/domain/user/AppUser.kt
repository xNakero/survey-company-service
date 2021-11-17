package pl.wat.surveycompanyservice.domain.user

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import pl.wat.surveycompanyservice.domain.role.Role
import javax.persistence.CascadeType.ALL
import javax.persistence.Entity
import javax.persistence.FetchType.EAGER
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Entity
@Table(name = "app_user")
class AppUser(
    @Id @GeneratedValue val userId: Long = 0L,
    private val username: String,
    private val password: String,
    @ManyToMany(cascade = [ALL], fetch = EAGER)
    @JoinTable(name = "user_role", joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "role_id")])
    private val roles: MutableSet<Role>
    ) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        roles.map { role -> SimpleGrantedAuthority(role.name) }.toMutableSet()

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}