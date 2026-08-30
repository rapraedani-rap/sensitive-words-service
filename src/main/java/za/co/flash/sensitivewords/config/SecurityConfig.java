package za.co.flash.sensitivewords.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${internal.auth.admin.username}")
    private String adminUsername;

    @Value("${internal.auth.admin.password}")
    private String adminPassword;

    @Value("${internal.auth.user.username}")
    private String userUsername;

    @Value("${internal.auth.user.password}")
    private String userPassword;


    @Bean
    public UserDetailsService userDetailsService() {

        return new InMemoryUserDetailsManager(User.withUsername(adminUsername)
                        .password(adminPassword)
                        .roles("ADMIN")
                        .build(),

                User.withUsername(userUsername)
                        .password(userPassword)
                        .roles("USER")
                        .build()
        );
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/v1/sanitize")
                        .hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/api/v1/sensitive-words/**")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }


    @Bean
    @SuppressWarnings("deprecation")
    public static NoOpPasswordEncoder passwordEncoder() {

        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }
}