package com.thesurvey.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thesurvey.api.exception.AuthenticationEntryPointHandler;
import com.thesurvey.api.exception.ErrorMessage;
import com.thesurvey.api.exception.mapper.NotFoundExceptionMapper;
import com.thesurvey.api.repository.UserRepository;
import com.thesurvey.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserRepository userRepository;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter(
                authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)), userService, objectMapper);
        loginAuthenticationFilter.setFilterProcessesUrl("/auth/login");

        // @formatter:off
        return http
                .csrf().disable()
                .cors().and()
                .authorizeRequests()
                .antMatchers(
                        "/configuration/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/docs/**"
                ).permitAll()
                .antMatchers("/admin/**").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.GET, "/surveys").permitAll()
                .antMatchers("/surveys").authenticated()
                .antMatchers("/surveys/**").authenticated()
                .antMatchers("/users/**").authenticated()
                .antMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                .anyRequest().permitAll()
                .and()
                .formLogin().disable()
                .addFilter(loginAuthenticationFilter)
                .exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPointHandler())
                .and()
                .logout()
                    .logoutUrl("/auth/logout")
                    .permitAll()
                    .invalidateHttpSession(true)
                    .logoutSuccessHandler(logoutSuccessHandler())
                .and()
                .sessionManagement()
                .sessionFixation().changeSessionId()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionConcurrency(configurer -> {
                    configurer.maximumSessions(1);
                    configurer.maxSessionsPreventsLogin(true);
                })
                .and()
                .build();
        // @formatter:on
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Arrays.asList("Cache-Control", "Content-Type"));

        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundExceptionMapper(ErrorMessage.USER_NAME_NOT_FOUND, email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> response.setStatus(HttpServletResponse.SC_OK);
    }
}
