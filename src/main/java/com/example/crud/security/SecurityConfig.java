package com.example.crud.security;

import com.example.crud.dto.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Autowired // Autowire the CorsConfigurationSource bean
    private CorsConfigurationSource corsConfigurationSource;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Apply CORS configuration FIRST
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Configure CSRF - Disable for specific paths
                .csrf(csrf -> csrf
                                .ignoringRequestMatchers(
                                        new AntPathRequestMatcher("/GP/car/prediction"),
                                        new AntPathRequestMatcher("/GP/car/ultrasonic") // Likely needs ignoring too if called by device
                                        // Add any other machine-to-machine POST/PUT/DELETE endpoints here
                                )
                        // If *all* car endpoints are machine-to-machine, you could use:
                        // .ignoringRequestMatchers(new AntPathRequestMatcher("/GP/car/**"))
                )

                .authorizeHttpRequests(configurer -> configurer
                        // WebSocket
                        .requestMatchers("/GP/ws/**").permitAll()

                        // Public POST endpoints
                        .requestMatchers(HttpMethod.POST,
                                "/GP/ws/broadcast", "/GP/ws/closeAll", "/GP/signup", "/GP/login",
                                "/GP/verifyEmail", "/GP/forgot_password", "/GP/reset_password",
                                "/GP/confirm_reset_code", "/GP/resendForgot", "/GP/car/ultrasonic",
                                "/GP/emergency/location", "/GP/car/prediction"
                        ).permitAll()

                        // Admin-only
                        .requestMatchers(HttpMethod.GET, "/GP/admin/users").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/GP/admin/users/{Id}").hasAuthority("ROLE_ADMIN")

                        // Authenticated endpoints
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

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

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
}
