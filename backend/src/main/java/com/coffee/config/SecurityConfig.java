package com.coffee.config;

import com.coffee.security.CustomUserDetailsService;
import com.coffee.security.JwtRequestFilter;
import com.coffee.security.UnauthorizedHandler;
import com.coffee.service.InventorySnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.resource.PathResourceResolver;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
@EnableScheduling
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final UnauthorizedHandler unauthorizedHandler;
    @Autowired
    private InventorySnapshotService snapshotService;

//    // Run at 23:59 every day
//    @Scheduled(cron = "0 59 23 * * *")
    @Scheduled(cron = "0 0 0 * * ?") // Chạy lúc 12h đêm hàng ngày
    public void scheduleSnapshotCreation() {
        snapshotService.createDailySnapshot();
    }

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtRequestFilter jwtRequestFilter, UnauthorizedHandler unauthorizedHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.avatar-dir}")
    private String avatarDir;

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Product images path
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Avatar path
        Path avatarPath = Paths.get(avatarDir).toAbsolutePath().normalize();

        // Create directories if they don't exist
        try {
            Files.createDirectories(uploadPath);
            Files.createDirectories(avatarPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories!", e);
        }

        registry.addResourceHandler("/uploads/**", "/images/**", "/uploads/images/**", "/avatars/**","/uploads/avatars/**")
                .addResourceLocations(
                        uploadPath.toUri().toString(),
                        avatarPath.toUri().toString(),
                        "file:./uploads/",
                        "file:./uploads/avatars/"
                )
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/user/signup",
                                "/api/v1/user/login",
                                "/api/v1/user/forgotPassword",
                                "/uploads/**",
                                "/images/**",
                                "/avatars/**",
                                "/api/v1/product/images/**",
                                "/api/v1/product/get",
                                "/api/v1/category/get",
                                "/api/v1/product/getByCategory/**",
                                "/api/v1/product/getById/**",
                                "/api/v1/product/search",
                                "/api/v1/product/related/**",
//                                "/api/v1/user/profile",
//                                "/api/v1/user/avatar",
                                "/api/v1/user/avatars/**",
                                "/api/v1/inventory/status/**"
                        ).permitAll()
                        .requestMatchers("/api/**").authenticated()
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

