package com.student.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 前端静态资源
                .requestMatchers(
                    "/", "/index.html", "/favicon.svg", "/icons.svg",
                    "/assets/**"
                ).permitAll()
                // 认证接口
                .requestMatchers("/api/v1/auth/**").permitAll()
                // 管理员登录接口
                .requestMatchers("/api/v1/admin/auth/login").permitAll()
                // 公开的 GET 接口
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/posts/**",
                    "/api/v1/trades/**",
                    "/api/v1/user/*/posts",
                    "/api/v1/user/*/trades"
                ).permitAll()
                // 文件上传接口
                .requestMatchers("/api/v1/upload/**").permitAll()
                // 静态资源（上传的图片等）
                .requestMatchers("/uploads/**").permitAll()
                // WebSocket
                .requestMatchers("/ws/**").permitAll()
                // 管理后台接口需要 ADMIN 角色
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
