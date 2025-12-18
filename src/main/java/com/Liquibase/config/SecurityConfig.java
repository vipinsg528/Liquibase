package com.Liquibase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManagerFactories;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // FIX 1: Change generic type to RequestAuthorizationContext for Servlet compatibility
        var mfa = AuthorizationManagerFactories.<RequestAuthorizationContext>multiFactor()
                .requireFactors(FactorGrantedAuthority.PASSWORD_AUTHORITY)
                .build();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // .access() now receives the correctly typed MFA manager
                        .requestMatchers("/api/employees/admin/**").access(mfa.hasRole("ADMIN"))
                        .requestMatchers("/api/employees/**").access(mfa.hasAllRoles("USER", "ADMIN"))
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // User 1: John Doe
        UserDetails john = User.builder()
                .username("john")
                .password("{noop}user")
                .roles("USER")
                .build();

        // User 2: Jane Smith (Admin)
        UserDetails jane = User.builder()
                .username("jane")
                .password("{noop}user")
                .roles("ADMIN", "USER")
                .build();

        // User 3: Bob Johnson
        UserDetails bob = User.builder()
                .username("bob")
                .password("{noop}user")
                .roles("USER")
                .build();

        // FIX 2: Use InMemoryUserDetailsManager instead of MapReactiveUserDetailsService
        return new InMemoryUserDetailsManager(john, jane, bob);
    }
}