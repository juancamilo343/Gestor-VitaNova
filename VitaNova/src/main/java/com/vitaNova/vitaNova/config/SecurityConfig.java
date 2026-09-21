package com.vitaNova.vitaNova.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // ==========================
                        // PÚBLICO
                        // ==========================
                        .requestMatchers(
                                "/login",
                                "/registro",
                                "/registro/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()


                        // ==========================
                        // TODOS LOS EMPLEADOS
                        // PUEDEN VISUALIZAR
                        // ==========================

                        // EMPLEADOS
                        .requestMatchers(
                                "/view/empleados/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // USUARIOS
                        .requestMatchers(
                                "/view/usuarios/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // ROLES
                        .requestMatchers(
                                "/view/rol/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // CLIENTES
                        .requestMatchers(
                                "/view/clientes/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // PRODUCTOS
                        .requestMatchers(
                                "/view/productos/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // CATEGORÍAS
                        .requestMatchers(
                                "/view/categorias/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // INVENTARIO
                        .requestMatchers(
                                "/view/inventario/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // VENTAS
                        .requestMatchers(
                                "/view/ventas/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // COMPRAS
                        .requestMatchers(
                                "/view/compras/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // PROVEEDORES
                        .requestMatchers(
                                "/view/proveedores/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // REPORTES
                        .requestMatchers(
                                "/view/reportes/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // DASHBOARD
                        .requestMatchers(
                                "/view/dashboard"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR",
                                "AUXILIAR_FARMACIA"
                        )


                        // ==========================
                        // CUALQUIER OTRA RUTA
                        // ==========================
                        .anyRequest().authenticated()
                )


                // ==========================
                // LOGIN
                // ==========================
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/view/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )


                // ==========================
                // LOGOUT
                // ==========================
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}