package fr.backendt.cinephobia.configurations;

import fr.backendt.cinephobia.services.UserDetailsServiceImpl;
import io.github.wimdeblauwe.htmx.spring.boot.security.HxRefreshHeaderAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SpringSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SpringSecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        HxRefreshHeaderAuthenticationEntryPoint hxAuthEntry = new HxRefreshHeaderAuthenticationEntryPoint();
        RequestMatcher htmxHeaderMatcher = new RequestHeaderRequestMatcher("HX-Request");
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/register", "/login", "/webjars/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/"))
                .exceptionHandling(exception -> exception // Full page refresh on auth timeout
                        .defaultAuthenticationEntryPointFor(hxAuthEntry, htmxHeaderMatcher));
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(this.userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() { // Enable thymeleaf support for spring security
        return new SpringSecurityDialect();
    }

}
