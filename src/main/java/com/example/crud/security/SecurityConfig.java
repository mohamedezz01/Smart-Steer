package com.example.crud.security;
import com.example.crud.dto.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(configurer ->
                        configurer
                                .requestMatchers(HttpMethod.POST,
                                        "/GP/signup",
                                        "/GP/login",
                                        "/GP/verifyEmail",
                                        "/GP/forgot_password",
                                        "/GP/reset_password",
                                        "/GP/confirm_reset_code",
                                        "/GP/resendForgot"
                                ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/GP/admin/users").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/GP/admin/users/{Id}").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.POST,
                                        "/GP/emergency/add",
                                        "/GP/settings/logout",
                                        "/GP/settings/verify_delAcc",
                                        "/GP/settings/confirmCurrentEmail",
                                        "/GP/settings/verifyCurrentEmail",
                                        "/GP/settings/sendNewEmailVerification",
                                        "/GP/settings/confirmNewEmail",
                                        "/GP/resendVerification",
                                        "/GP/settings/uploadProfilePicture",
                                        "/GP/resendForgot"
                                ).hasAnyAuthority("ROLE_USER","ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET,
                                        "/GP/users",
                                        "/GP/users/**",
                                        "/GP/emergency/list",
                                        "/GP/settings/email",
                                        "/GP/settings/profilePicture"
                                ).hasAnyAuthority("ROLE_USER","ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT,
                                        "/GP/users/**",
                                        "/GP/emergency/update/{contactId}",
                                        "/GP/settings/changeEmail",
                                        "/GP/settings/changePassword"
                                ).hasAnyAuthority("ROLE_USER","ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE,
                                        "/GP/users/**",
                                        "/GP/emergency/delete/{contactId}",
                                        "/GP/settings/confirm_delAcc"
                                ).hasAnyAuthority("ROLE_USER","ROLE_ADMIN")
                                .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
