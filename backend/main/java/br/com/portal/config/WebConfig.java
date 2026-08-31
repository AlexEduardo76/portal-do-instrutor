package br.com.portal.config;
import org.springframework.context.annotation.*; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.servlet.config.annotation.*;
@Configuration public class WebConfig implements WebMvcConfigurer { @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);} }
