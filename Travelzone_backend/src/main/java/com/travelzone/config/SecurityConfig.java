package com.travelzone.config;

import com.travelzone.security.CustomUserDetailsService;
import com.travelzone.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService customUserDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints ─────────────────────────────────────
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/guides").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/guides/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hotels").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()

                        // ── Admin ─────────────────────────────────────────────────
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // ── Payments ─────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/payments")
                                .hasAuthority("ROLE_TOURIST")
                        .requestMatchers(HttpMethod.GET, "/api/payments/my-payments")
                                .hasAuthority("ROLE_TOURIST")
                        .requestMatchers(HttpMethod.GET, "/api/payments/guide-payments")
                                .hasAuthority("ROLE_GUIDE")
                        .requestMatchers(HttpMethod.GET, "/api/payments/hotel-payments")
                                .hasAuthority("ROLE_HOTEL_OWNER")

                        // ── Reviews ──────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/reviews")
                                .hasAuthority("ROLE_TOURIST")
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/**")
                                .hasAuthority("ROLE_TOURIST")
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**")
                                .hasAuthority("ROLE_TOURIST")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/my-reviews")
                                .hasAuthority("ROLE_TOURIST")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/guide-reviews")
                                .hasAuthority("ROLE_GUIDE")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/hotel-reviews")
                                .hasAuthority("ROLE_HOTEL_OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/reviewable-guide-bookings")
                                .hasAuthority("ROLE_TOURIST")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/reviewable-reservations")
                                .hasAuthority("ROLE_TOURIST")

                        // ── Everything else requires authentication ───────────────
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}