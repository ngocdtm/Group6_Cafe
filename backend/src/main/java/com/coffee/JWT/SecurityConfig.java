package com.coffee.JWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

//class này cấu hình toàn bộ hệ thống bảo mật cho ứng dụng, bao gồm cách xác thực người dùng, quản lý phiên
// xử lý các yêu cầu HTTP
// Nó sử dụng JWT (JSON Web Token) để xác thực và duy trì trạng thái người dùng.

@Configuration//nguồn cấu hình cho Spring
@EnableWebSecurity//kích hoạt bảo mật web
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customerUsersDetailsService);
    }

    @Bean // mã hóa mật khẩu
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues())
                .and().
                csrf().disable()
                .authorizeRequests()
                .antMatchers("/user/login","/user/forgotPassword","/user/signup","/user/get","/user/update","/user/changePassword",
                        "/category/add","/category/get","/category/update","/product/add","/product/get","/product/update",
                        "/product/delete/**","/product/updateStatus","/product/getByCategory/**","/product/getById/**","/bill/generateReport",
                        "/bill/getBills","/bill/getPdf","/bill/delete/**","/dashboard/details")
                .permitAll()
                .anyRequest().
                authenticated()
                .and().exceptionHandling()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

}