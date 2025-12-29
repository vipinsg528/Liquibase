//package com.Liquibase.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.*;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtFilter;
//
//    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authorizeHttpRequests(auth -> auth
//
//                        // 🔓 AUTH APIs
//                        .requestMatchers("/auth/**").permitAll()
//
//                        // 🔐 ADMIN ONLY
//                        .requestMatchers("/api/employees/admin/**")
//                        .hasRole("ADMIN")
//
//                        // 🔐 USER + ADMIN
//                        .requestMatchers("/api/employees/**")
//                        .hasAnyRole("USER", "ADMIN")
//
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(
//                        jwtFilter,
//                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
//                );
//
//        return http.build();
//    }
//
//    /**
//     * 🔥 THIS BEAN WAS MISSING
//     * Required for AuthController
//     */
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authenticationConfiguration
//    ) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//}


package com.Liquibase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                /* ---------------- CORS ---------------- */
                .cors(Customizer.withDefaults())

                /* ---------------- CSRF ---------------- */
                .csrf(AbstractHttpConfigurer::disable)

                /* ---------------- SESSION ----------------
                   IF request has JWT → stateless
                   IF form login → session based
                */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /* ---------------- AUTHORIZATION ---------------- */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/employees/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/employees/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )

                /* ---------------- FORM LOGIN ---------------- */
                .formLogin(form -> form
                        .loginPage("/login")              // optional custom page
                        .defaultSuccessUrl("/api/employees/get", true)
                        .permitAll()
                )

                /* ---------------- LOGOUT ---------------- */
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                /* ---------------- JWT FILTER ---------------- */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /* ---------------- AUTH MANAGER ---------------- */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /* ---------------- CORS CONFIG ---------------- */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
