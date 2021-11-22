package pl.wat.surveycompanyservice.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import pl.wat.surveycompanyservice.domain.role.AppRole.PARTICIPANT
import pl.wat.surveycompanyservice.domain.role.AppRole.RESEARCHER
import pl.wat.surveycompanyservice.infrastructure.filter.AuthenticationFilter
import pl.wat.surveycompanyservice.infrastructure.token.TokenService


@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
open class WebSecurityConfig(
    private val userService: UserDetailsService,
    private val tokenService: TokenService,
    private val objectMapper: ObjectMapper
): WebSecurityConfigurerAdapter(){

    override fun configure(http: HttpSecurity?) {
        http!!
            .csrf().disable()
            .cors().disable()
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(
                    "renew",
                    "personal-profile",
                    "survey",
                    "survey/participants-count"
                ).authenticated()
                .antMatchers(
                    "personal-profile"
                ).hasRole(PARTICIPANT.toString())
                .antMatchers(
                    "survey",
                    "survey/participants-count"
                ).hasRole(RESEARCHER.toString())
            .and()
            .addFilter(AuthenticationFilter(authenticationManager(), tokenService, objectMapper))
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!
            .userDetailsService(userService)
            .passwordEncoder(passwordEncoder())
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/register", "/login")
    }

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager =
        super.authenticationManagerBean()


    @Bean
    open fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder(10)
}