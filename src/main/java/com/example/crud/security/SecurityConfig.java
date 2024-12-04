package com.example.crud.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(configurer ->
                configurer
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/GP/signup", "/GP/login", "/GP/verifyEmail",
                                "/GP/forgot_password", "/GP/reset_password","/GP/emergency/add").permitAll()
                        .requestMatchers(HttpMethod.GET, "/GP/users", "/GP/users/**","/GP/emergency/list").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/GP/users","/GP/emergency/update/{contactId}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/GP/users/**","/GP/emergency/delete/{contactId}").permitAll()
                        // Default rule for all other endpoints
                        .anyRequest().authenticated());
                        // Use HTTP Basic Auth for simplicity (can be customized for JWT)
                        http.httpBasic(Customizer.withDefaults());
                        // Disable CSRF for APIs
                         http.csrf(csrf -> csrf.disable());
                          return http.build();
                 }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://192.168.1.5"); // Replace with your mobile app's IP
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
