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
                        .requestMatchers(HttpMethod.POST, "/GP/signup", "/GP/login", "/GP/verifyEmail","/GP/settings/verify_delAcc",
                                "/GP/forgot_password", "/GP/reset_password","/GP/emergency/add","/GP/confirm_reset_code","/GP/settings/logout",
                                "/GP/settings/confirmCurrentEmail","/GP/settings/verifyCurrentEmail","/GP/settings/sendNewEmailVerification","/GP/settings/confirmNewEmail","/GP/resendVerification","/GP/settings/uploadProfilePicture").permitAll()
                        .requestMatchers(HttpMethod.GET, "/GP/users", "/GP/users/**","/GP/emergency/list","/GP/settings/email","/GP/settings/profilePicture").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/GP/users","/GP/emergency/update/{contactId}","/GP/settings/changeEmail","/GP/settings/changePassword").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/GP/users/**","/GP/emergency/delete/{contactId}","/GP/settings/confirm_delAcc").permitAll()
                        .requestMatchers(HttpMethod.PATCH,"/GP/settings/updateInfo").permitAll()
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
        configuration.addAllowedOriginPattern("*"); // Debugging purpose, allow all origins

//        configuration.addAllowedOrigin("http://192.168.1.9");
//        configuration.addAllowedOrigin("http://192.168.1.9:8080"); //replace with the mobile app iP and port
//        configuration.addAllowedOrigin("http://localhost:8080"); //allows localhost for development
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
