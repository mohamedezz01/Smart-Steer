package com.example.crud.security;

import com.example.crud.dto.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // Chain 0: For WebSocket connections - HIGHEST PRECEDENCE
    @Bean
    @Order(0)
    public SecurityFilterChain websocketFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new AntPathRequestMatcher("/GP/ws/**")) // More specific matcher
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // Permit all for WebSocket path
                )
                .httpBasic(AbstractHttpConfigurer::disable) // Disable HTTP Basic FOR THIS CHAIN
                .csrf(AbstractHttpConfigurer::disable) // Often disable CSRF for WebSockets or stateless APIs
                // .csrf(csrf -> csrf.ignoringRequestMatchers("/GP/ws/**")) // Alternative CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults());
        return http.build();
    }

    // Chain 1: For other explicitly public HTTP endpoints
    @Bean
    @Order(1)
    public SecurityFilterChain publicHttpEndpointsFilterChain(HttpSecurity http) throws Exception {
        // Consolidate all public HTTP POST and GET endpoints here
        AntPathRequestMatcher[] publicMatchers = Stream.of(
                // Public POST endpoints from your original config
                new AntPathRequestMatcher("/GP/ws/broadcast", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/ws/closeAll", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/signup", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/login", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/verifyEmail", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/forgot_password", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/reset_password", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/confirm_reset_code", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/resendForgot", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/car/ultrasonic", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/emergency/location", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/GP/car/prediction", HttpMethod.POST.name())
                // Add any other public GET/POST/PUT/DELETE paths here explicitly
        ).toArray(AntPathRequestMatcher[]::new);

        http
                .securityMatcher(Arrays.toString(publicMatchers))
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable); // Disable HTTP Basic FOR THIS CHAIN
        return http.build();
    }

    // Chain 2: For secured endpoints (everything else)
    @Bean
    @Order(2)
    public SecurityFilterChain securedEndpointsFilterChain(HttpSecurity http) throws Exception {
        http
                // No specific securityMatcher here, so it applies to everything not caught by Order 0 or 1
                .authorizeHttpRequests(configurer -> configurer
                        // Admin-only
                        .requestMatchers(HttpMethod.GET, "/GP/admin/users").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/GP/admin/users/{Id}").hasAuthority("ROLE_ADMIN")

                        // Authenticated endpoints (copied from your original config)
                        .requestMatchers(HttpMethod.POST,
                                "/GP/emergency/add", "/GP/emergency/alert", "/GP/settings/logout",
                                "/GP/settings/verify_delAcc", "/GP/settings/confirmCurrentEmail",
                                "/GP/settings/verifyCurrentEmail", "/GP/settings/sendNewEmailVerification",
                                "/GP/settings/confirmNewEmail", "/GP/settings/serialNumber",
                                "/GP/resendVerification", "/GP/settings/uploadProfilePicture"
                        ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET,
                                "/GP/users", "/GP/users/**", "/GP/emergency/list",
                                "/GP/settings/email", "/GP/settings/profilePicture",
                                "/GP/tech/profilePicture/byPost"
                        ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.PUT,
                                "/GP/users/**", "/GP/settings/changeEmail",
                                "/GP/settings/changePassword", "/GP/emergency/update/{contactId}"
                        ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.DELETE,
                                "/GP/users/**", "/GP/emergency/delete/{contactId}",
                                "/GP/settings/confirm_delAcc"
                        ).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        // Tech Post Feature
                        .requestMatchers(HttpMethod.POST, "/GP/tech/posts").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/GP/tech/posts", "/GP/tech/posts/{id}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/GP/tech/posts/{id}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.POST, "/GP/tech/likes").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/GP/tech/likes/{postId}/{userId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.POST, "/GP/tech/comments").hasAnyAuthority("ROLE_ADMIN", "ROLE_OWNER", "ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/GP/tech/comments/post/{postId}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/GP/tech/username").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/GP/tech/comments/{id}").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER")
                        .anyRequest().authenticated() // All other requests must be authenticated
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults()) // Enable HTTP Basic for this chain (as a fallback if JWT fails)
                .csrf(AbstractHttpConfigurer::disable) // Consider configuring CSRF properly for stateful parts if any
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // For dev only
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Bean to provide a UserDetailsService to Spring Boot
    // This helps prevent the auto-configuration of a default in-memory user and generated password.
    @Bean
    public UserDetailsService dummyUserDetailsService() {
        return username -> {
            // This is a placeholder. For JWT-based auth, the JwtRequestFilter handles authentication.
            // If you had other forms of username/password auth, you'd implement actual user lookup here.
            throw new UsernameNotFoundException("User not found: " + username + ". (This is expected if only JWT is used for user auth)");
        };
    }

    // Bean to provide an AuthenticationManager
    // Spring Boot will use this instead of creating a default one.
    // It will be configured with the UserDetailsService above (and PasswordEncoder).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}