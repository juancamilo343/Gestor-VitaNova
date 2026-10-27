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
                                "/uploads/**"
                        ).permitAll()


                        // ==========================
                        // DASHBOARD
                        // Acceso: Administrador o usuarios con PERM_DASHBOARD
                        // ==========================
                        .requestMatchers(
                                "/view",
                                "/view/",
                                "/view/dashboard",
                                "/view/dashboard/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_DASHBOARD"
                        )


                        // ==========================
                        // VENTAS
                        // Acceso: Administrador o usuarios con PERM_VENTAS
                        // ==========================
                        .requestMatchers(
                                "/view/ventas/**",
                                "/api/ventas/**",
                                "/api/detalles-venta/**",
                                "/legacy/ventas/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_VENTAS"
                        )


                        // ==========================
                        // CLIENTES
                        // Acceso: Administrador o usuarios con PERM_CLIENTES
                        // ==========================
                        .requestMatchers(
                                "/view/clientes/**",
                                "/api/clientes/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_CLIENTES"
                        )


                        // ==========================
                        // PRODUCTOS
                        // Acceso: Administrador o usuarios con PERM_PRODUCTOS
                        // ==========================
                        .requestMatchers(
                                "/view/productos/**",
                                "/api/productos/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_PRODUCTOS"
                        )


                        // ==========================
                        // INVENTARIO
                        // Acceso: Administrador o usuarios con PERM_INVENTARIO
                        // ==========================
                        .requestMatchers(
                                "/view/inventario/**",
                                "/api/inventario/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_INVENTARIO"
                        )


                        // ==========================
                        // LOTES (INVENTARIO)
                        // Acceso: Administrador o usuarios con PERM_LOTES
                        // ==========================
                        .requestMatchers(
                                "/view/lotes/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_LOTES"
                        )


                        // ==========================
                        // CATEGORÍAS
                        // Acceso: Administrador o usuarios con PERM_CATEGORIAS
                        // ==========================
                        .requestMatchers(
                                "/view/categorias/**",
                                "/api/categorias/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_CATEGORIAS"
                        )


                        // ==========================
                        // COMPRAS
                        // Acceso: Administrador o usuarios con PERM_COMPRAS
                        // ==========================
                        .requestMatchers(
                                "/view/compras/**",
                                "/api/compras/**",
                                "/api/detalles-compra/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_COMPRAS"
                        )


                        // ==========================
                        // PROVEEDORES
                        // Acceso: Administrador o usuarios con PERM_PROVEEDORES
                        // ==========================
                        .requestMatchers(
                                "/view/proveedores/**",
                                "/api/proveedores/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_PROVEEDORES"
                        )


                        // ==========================
                        // REPORTES
                        // Acceso: Administrador o usuarios con PERM_REPORTES
                        // ==========================
                        .requestMatchers(
                                "/view/reportes/**",
                                "/api/reportes/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_REPORTES"
                        )


                        // ==========================
                        // EMPLEADOS
                        // Acceso: Administrador o usuarios con PERM_EMPLEADOS
                        // ==========================
                        .requestMatchers(
                                "/view/empleados/**",
                                "/api/empleados/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR",
                                "PERM_EMPLEADOS"
                        )


                        // ==========================
                        // USUARIOS
                        // Exclusivo: Administrador con PERM_USUARIOS
                        // ==========================
                        .requestMatchers(
                                "/view/usuarios/**",
                                "/api/usuarios/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR"
                        )


                        // ==========================
                        // ROLES
                        // Exclusivo: Administrador con PERM_ROLES
                        // ==========================
                        .requestMatchers(
                                "/view/rol/**",
                                "/api/roles/**"
                        ).hasAnyAuthority(
                                "ROLE_ADMINISTRADOR"
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
