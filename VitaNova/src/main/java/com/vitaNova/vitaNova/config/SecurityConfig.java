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
                        // DASHBOARD
                        // Acceso: Administrador, Farmacéutico, Vendedor
                        // ==========================
                        .requestMatchers(
                                "/view",
                                "/view/",
                                "/view/dashboard",
                                "/view/dashboard/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR"
                        )


                        // ==========================
                        // VENTAS
                        // Acceso: Administrador, Farmacéutico, Vendedor
                        // ==========================
                        .requestMatchers(
                                "/view/ventas/**",
                                "/api/ventas/**",
                                "/api/detalles-venta/**",
                                "/legacy/ventas/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR"
                        )


                        // ==========================
                        // CLIENTES
                        // Acceso: Administrador, Farmacéutico, Vendedor
                        // ==========================
                        .requestMatchers(
                                "/view/clientes/**",
                                "/api/clientes/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR"
                        )


                        // ==========================
                        // PRODUCTOS
                        // Acceso: Administrador, Farmacéutico, Vendedor
                        // (El vendedor puede consultar productos para ventas)
                        // ==========================
                        .requestMatchers(
                                "/view/productos/**",
                                "/api/productos/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR"
                        )


                        // ==========================
                        // INVENTARIO
                        // Acceso: Administrador, Farmacéutico, Vendedor
                        // (El vendedor consulta disponibilidad de stock)
                        // ==========================
                        .requestMatchers(
                                "/view/inventario/**",
                                "/api/inventario/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO",
                                "VENDEDOR"
                        )


                        // ==========================
                        // LOTES (INVENTARIO)
                        // Acceso: Administrador, Farmacéutico
                        // ==========================
                        .requestMatchers(
                                "/view/lotes/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO"
                        )


                        // ==========================
                        // CATEGORÍAS
                        // Acceso: Administrador, Farmacéutico
                        // ==========================
                        .requestMatchers(
                                "/view/categorias/**",
                                "/api/categorias/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO"
                        )


                        // ==========================
                        // COMPRAS
                        // Acceso: Administrador, Farmacéutico
                        // ==========================
                        .requestMatchers(
                                "/view/compras/**",
                                "/api/compras/**",
                                "/api/detalles-compra/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO"
                        )


                        // ==========================
                        // PROVEEDORES
                        // Acceso: Administrador, Farmacéutico
                        // ==========================
                        .requestMatchers(
                                "/view/proveedores/**",
                                "/api/proveedores/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO"
                        )


                        // ==========================
                        // REPORTES
                        // Acceso: Administrador, Farmacéutico
                        // ==========================
                        .requestMatchers(
                                "/view/reportes/**",
                                "/api/reportes/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO"
                        )


                        // ==========================
                        // EMPLEADOS
                        // Acceso: Administrador, Farmacéutico
                        // ==========================
                        .requestMatchers(
                                "/view/empleados/**",
                                "/api/empleados/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "FARMACEUTICO"
                        )


                        // ==========================
                        // USUARIOS
                        // Exclusivo: Administrador
                        // ==========================
                        .requestMatchers(
                                "/view/usuarios/**",
                                "/api/usuarios/**"
                        ).hasRole("ADMINISTRADOR")


                        // ==========================
                        // ROLES
                        // Exclusivo: Administrador
                        // ==========================
                        .requestMatchers(
                                "/view/rol/**",
                                "/api/roles/**"
                        ).hasRole("ADMINISTRADOR")


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