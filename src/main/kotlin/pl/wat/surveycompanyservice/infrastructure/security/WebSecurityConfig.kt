package pl.wat.surveycompanyservice.infrastructure.security

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
import pl.wat.surveycompanyservice.domain.role.AppRole.INTERVIEWEE
import pl.wat.surveycompanyservice.infrastructure.filter.AuthenticationFilter
import pl.wat.surveycompanyservice.infrastructure.token.TokenService

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
open class WebSecurityConfig(
    private val userService: UserDetailsService,
    private val tokenService: TokenService
): WebSecurityConfigurerAdapter(){

    override fun configure(http: HttpSecurity?) {
        http!!
            .csrf().disable()
            .cors().disable()
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/renew").authenticated()
                .antMatchers("/personal-profile").authenticated()
                .antMatchers("/personal-profile", "/personal-profile/*").hasRole(INTERVIEWEE.toString())
            .and()
            .addFilter(AuthenticationFilter(authenticationManager(), tokenService))
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