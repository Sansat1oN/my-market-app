package ru.yandex.practicum.mymarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import ru.yandex.practicum.mymarket.repository.CustomerRepository;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange(routes -> routes
                        .pathMatchers(HttpMethod.GET, "/", "/items", "/items/{id}", "/images/{name}").permitAll()
                        .anyExchange().authenticated())
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults())
                .build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(CustomerRepository customerRepository) {
        return username -> customerRepository.findById(username)
                .map(customer -> User.withUsername(customer.getUsername())
                        .password(customer.getPasswordHash())
                        .roles("USER")
                        .build());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
