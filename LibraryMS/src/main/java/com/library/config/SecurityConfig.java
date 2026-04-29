package com.library.config;

import com.library.security.LibraryAuthenticationProvider;
import com.library.security.LibraryLoginFailureHandler;
import com.library.security.LibraryLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LibraryAuthenticationProvider libraryAuthenticationProvider,
            LibraryLoginSuccessHandler successHandler,
            LibraryLoginFailureHandler failureHandler)
            throws Exception {
        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/css/**",
                                                "/js/**",
                                                "/images/**",
                                                "/webjars/**",
                                                "/",
                                                "/login",
                                                "/register",
                                                "/register/complete",
                                                "/register/complete/**",
                                                "/error",
                                                "/access-denied")
                                        .permitAll()
                                        .requestMatchers("/uploads/profiles/**")
                                        .authenticated()
                                        .requestMatchers("/books/search/**")
                                        .hasAnyRole("LIBRARIAN", "STUDENT")
                                        .requestMatchers("/books/**")
                                        .hasRole("LIBRARIAN")
                                        .requestMatchers("/borrow/pending", "/borrow/approve/**", "/borrow/reject/**")
                                        .hasRole("LIBRARIAN")
                                        .requestMatchers("/borrow/request")
                                        .hasRole("STUDENT")
                                        .requestMatchers("/admin/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/librarian/**")
                                        .hasRole("LIBRARIAN")
                                        .requestMatchers("/student/**")
                                        .hasRole("STUDENT")
                                        .anyRequest()
                                        .authenticated())
                .authenticationProvider(libraryAuthenticationProvider)
                .formLogin(
                        form ->
                                form.loginPage("/login")
                                        .usernameParameter("email")
                                        .passwordParameter("password")
                                        .successHandler(successHandler)
                                        .failureHandler(failureHandler)
                                        .permitAll())
                .logout(
                        logout ->
                                logout.logoutUrl("/logout")
                                        .logoutSuccessUrl("/login?logout")
                                        .invalidateHttpSession(true)
                                        .deleteCookies("JSESSIONID")
                                        .permitAll())
                .exceptionHandling(
                        ex -> ex.accessDeniedPage("/access-denied"));

        return http.build();
    }
}
