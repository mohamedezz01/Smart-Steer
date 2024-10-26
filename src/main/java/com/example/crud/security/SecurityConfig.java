package com.example.crud.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig {


    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {

        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

        jdbcUserDetailsManager.setUsersByUsernameQuery(
                "select username, pass, enabled from users where username=?");

        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
                "select user_id,authority from authorities where user_id =?");

        return jdbcUserDetailsManager;
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(configurer ->
                configurer
                        .requestMatchers(HttpMethod.GET, "/GP/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/GP/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/GP/users").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/GP/users").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/GP/users/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/GP/login").permitAll()
                        .requestMatchers(HttpMethod.POST,"/GP/signup").permitAll()
                        .requestMatchers(HttpMethod.POST,"/GP/verifyEmail").permitAll()
        );


        // use HTTP Basic authentication
        http.httpBasic(Customizer.withDefaults());

        // disable Cross Site Request Forgery (CSRF)
        // in general, not required for stateless REST APIs that use POST, PUT, DELETE and/or PATCH
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}