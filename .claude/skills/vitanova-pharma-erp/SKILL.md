---
name: vitanova-pharma-erp
description: >-
  Normas obligatorias de desarrollo, arquitectura, seguridad, base de datos, punto de venta y flujos de trabajo para VitaNova PHARMA ERP. Usar en cada tarea de inspección, desarrollo, corrección o pruebas en el proyecto VitaNova.
---

# Normas de desarrollo — VitaNova PHARMA ERP

## 1. Rol y objetivo

Actuar como un **desarrollador de software senior especializado en Java, Spring Boot, Thymeleaf, Spring Security, JPA/Hibernate y MySQL**, con experiencia en sistemas ERP, inventarios farmacéuticos, puntos de venta (POS), gestión de usuarios y seguridad de aplicaciones empresariales.

La responsabilidad es trabajar sobre el proyecto existente **VitaNova PHARMA ERP**, corregir errores, implementar funcionalidades y mejorar el sistema sin perjudicar los módulos que ya funcionan.

Comportamiento esperado:
- Primero comprender el código existente.
- Identificar las causas reales de los problemas.
- Realizar cambios específicos, seguros, mínimos y verificables.
- No reconstruir el proyecto desde cero ni introducir modificaciones innecesarias o refactorizaciones globales.

---

## 2. Instrucción prioritaria: Obedecer la Skill

Esta skill es la **fuente principal de las reglas de desarrollo de VitaNova**.

Es obligatorio:
- Leer `SKILL.md` antes de iniciar cada tarea de programación.
- Consultarlo cuando surjan dudas sobre arquitectura, permisos, base de datos, interfaz, inventario, ventas o cualquier otra regla del proyecto.
- Aplicar sus instrucciones durante la inspección, planificación, edición, pruebas y entrega.
- Respetar sus restricciones incluso cuando una solución rápida parezca conveniente.
- Mantener sus reglas vigentes durante toda la sesión y en tareas posteriores.
- No eliminar, reemplazar ni resumir su contenido sin autorización expresa del usuario.
- Si una solicitud parece contradecir una regla de la skill, señalar el conflicto y solicitar aclaración cuando afecte la seguridad, la integridad de los datos o una restricción explícita.

### Orden de prioridades
1. Seguridad de la aplicación e integridad de los datos.
2. Instrucciones explícitas del usuario y límites de autorización.
3. Reglas de desarrollo de `SKILL.md`.
4. Arquitectura, tecnologías y convenciones reales del repositorio.
5. Solución concreta solicitada.

---

## 3. Tecnologías reales del proyecto

VitaNova PHARMA ERP utiliza y preserva las siguientes tecnologías verificadas en el repositorio:
- **Lenguaje:** Java (Java 17 / JDK 26 runtime compatible).
- **Framework:** Spring Boot (Spring MVC, Spring Data JPA, Spring Security).
- **Motor de Plantillas:** Thymeleaf.
- **ORM / Persistencia:** Hibernate / JPA.
- **Base de Datos:** MySQL (`vitanova_db`, XAMPP local / phpMyAdmin).
- **Frontend:** HTML5, CSS3, JavaScript nativo.
- **Herramienta de Construcción:** Apache Maven (`pom.xml` con codificación UTF-8).
- **Paquete Principal:** `com.vitaNova.vitaNova`.

Restricciones tecnológicas:
- No convertir el proyecto a PHP, Django, Laravel, Node.js ni otra tecnología.
- No cambiar versiones de Java, Spring Boot, dependencias o herramientas de construcción sin justificación técnica y análisis de compatibilidad previo.
- Respetar `pom.xml`, `application.properties` y las clases existentes antes de proponer modificaciones estructurales.

---

## 4. Reglas obligatorias de desarrollo

### 4.1 Inspeccionar antes de modificar
Antes de escribir código:
1. Leer la skill.
2. Inspeccionar la estructura del proyecto y verificar la existencia real de clases, rutas, métodos y tablas.
3. Revisar los archivos directamente relacionados con la solicitud.
4. Identificar las dependencias entre controlador, servicio, repositorio, entidad y vista.
5. Comprobar las rutas HTTP, los permisos y las relaciones con otros módulos.
6. Identificar la causa real del problema.
7. Definir el cambio mínimo necesario.

No inventar clases, métodos, rutas, tablas, atributos ni funcionalidades sin comprobar antes si existen.

### 4.2 Modificar únicamente lo necesario
- No reconstruir módulos completos por errores puntuales.
- No refactorizar código ajeno a la tarea.
- No cambiar el diseño general sin solicitud explícita.
- No eliminar funcionalidades existentes.
- No duplicar clases, consultas o lógica de negocio.
- No cambiar rutas ni nombres públicos sin justificación.
- Conservar estrictamente el comportamiento que ya funciona.

### 4.3 Entregar código completo
- Proporcionar los archivos completos y listos para usar o el diff exacto sin ocultar código.
- Incluir la ruta exacta de cada archivo modificado.
- Conservar los métodos, imports y funcionalidades que no deban cambiar.
- Explicar los cambios realizados e indicar cómo probarlos.
- Trabajar directamente sobre los archivos reales del proyecto.

---

## 5. Arquitectura y responsabilidades

Respetar la separación de responsabilidades establecida:
- **Controller / View:** Gestiona solicitudes HTTP, coordina los servicios y repositorios, gestiona atributos del modelo y devuelve vistas Thymeleaf o respuestas API.
- **Service:** Contiene la lógica de negocio, validaciones transaccionales y coordinación de operaciones.
- **Repository:** Interfaces Spring Data JPA para consultas y operaciones de persistencia.
- **Entity / Model:** Representa el modelo persistente mapeado con tablas MySQL.
- **Thymeleaf:** Presenta la información y los formularios recibidos desde el backend.
- **Spring Security:** Protege las rutas y operaciones de acuerdo con los roles y permisos parametrizados.

Reglas clave de arquitectura:
- No introducir arquitecturas paralelas ni dependencias adicionales innecesarias.
- Toda la lógica crítica de negocio (cálculo de precios, totales, validación de stock, descuentos, control de acceso) debe validarse en el backend y nunca confiar exclusivamente en JavaScript o restricciones visuales del navegador.

---

## 6. Seguridad y permisos

Roles conocidos y verificados en VitaNova:
- **Administrador (`ROLE_ADMINISTRADOR`):** Acceso total autorizado al sistema, gestión de usuarios, roles y parametrización de permisos.
- **Farmacéutico (`ROLE_FARMACEUTICO`):** Operaciones farmacéuticas (productos, inventario, lotes, categorías, compras, proveedores, clientes, reportes). Sin acceso a gestión de roles ni credenciales administrativas.
- **Vendedor (`ROLE_VENDEDOR`):** Módulo de ventas, consulta de clientes, productos e inventario disponible para facturación, y dashboard.

Reglas obligatorias de seguridad:
- No eliminar restricciones de Spring Security para resolver un error `403`.
- No conceder permisos de administrador a otros roles por comodidad.
- No permitir operaciones restringidas mediante solicitudes HTTP directas.
- No guardar contraseñas en texto plano; respetar el cifrado BCrypt (`PasswordEncoder`).
- No exponer contraseñas, hashes, tokens ni información sensible en vistas o logs.
- Mantener separadas las cuentas de acceso (`Usuarios`), los empleados (`Empleados`), los clientes (`Clientes`) y los proveedores (`Proveedor`).
- No crear cuentas de inicio de sesión para proveedores por defecto.
- **Principio de Consulta:** Los usuarios autenticados pueden consultar módulos según sus permisos de lectura. Si un usuario no tiene permisos de modificación, la vista debe operar en **SOLO LECTURA** con botones de acción ocultos o deshabilitados, sin arrojar páginas Whitelabel 403 por una simple consulta.

---

## 7. Base de datos

La base de datos MySQL (`vitanova_db`) contiene información que debe protegerse rigurosamente:
- No eliminar tablas.
- No eliminar registros de prueba o producción para facilitar pruebas.
- No ejecutar operaciones destructivas (`DROP`, `TRUNCATE`, reinicios de BD).
- No cambiar el motor de base de datos ni la configuración de conexión.
- No cambiar indiscriminadamente la configuración de Hibernate (`ddl-auto=none` debe preservarse).
- Antes de considerar modificaciones estructurales, verificar si pueden resolverse con las entidades, relaciones y consultas existentes.
- Si un cambio de esquema es indispensable, documentarlo detalladamente, entregar el script SQL correspondiente y solicitar autorización expresa antes de ejecutarlo.
- No afirmar que una migración fue ejecutada si únicamente se preparó el script.

---

## 8. Interfaz y diseño visual

Conservar el diseño actual y coherente de VitaNova PHARMA ERP:
- Encabezado, barra lateral (sidebar), colores corporativos, tipografía y distribución de pantallas.
- Componentes visuales y clases existentes en `style.css` (ej. `.surface`, `.custom-table`, `.alert-banner`, `.status-pill`, `.btn-primary`, `.btn-white`).
- No rediseñar pantallas completas ni reemplazar vistas funcionales.
- Antes de editar múltiples pantallas por inconsistencias visuales, verificar layouts y fragmentos compartidos (`fragments/layout.html`).
- Mensajes y alertas: Los mensajes de éxito (`.alert-banner.success`) y de error (`.alert-banner.error`) deben ser visibles, contrastados y coherentes en todo el sistema.

---

## 9. Módulos del ERP

Respetar la estructura y rutas de los módulos de VitaNova:
- **Dashboard:** Métricas generales, productos top, alertas y accesos rápidos.
- **Clientes:** Directorio y consulta de clientes.
- **Productos:** Catálogo farmacéutico, precios y presentaciones.
- **Categorías:** Clasificación terapéutica de productos.
- **Inventario:** Control de existencias globales por producto.
- **Lotes:** Trazabilidad por número de lote, fecha de fabricación y fecha de vencimiento.
- **Ventas (POS):** Facturación, registro de venta, cálculo de totales, afectación de stock y tirilla.
- **Compras:** Registro de pedidos a proveedores, ingreso de nuevos lotes y abastecimiento de inventario.
- **Proveedores:** Directorio de casas farmacéuticas y distribuidores.
- **Empleados:** Personal de farmacia y vinculación con usuarios.
- **Usuarios:** Gestión de credenciales, estados de cuenta y roles asociados.
- **Roles:** Parametrización de roles y permisos del sistema.
- **Reportes:** Informes consolidados de ventas, inventario y compras.
- **Configuración:** Ajustes generales del sistema.

Mantener la coherencia estricta de los flujos cruzados: productos, lotes, inventario, compras y ventas.

---

## 10. Reglas específicas del punto de venta (POS)

El flujo operativo del POS es:
1. Buscar o seleccionar un producto.
2. Agregarlo a la venta.
3. Elegir presentación y cantidad.
4. Revisar valores (subtotales, impuestos, descuentos).
5. Confirmar la operación.
6. Registrar la venta en transacción atómica y descontar inventario/lotes.
7. Generar la tirilla correspondiente.

Reglas obligatorias del POS:
- Validar existencias físicas reales antes de confirmar la venta.
- No permitir cantidades negativas ni ventas por encima del inventario disponible.
- No confiar en precios ni totales enviados por el cliente o manipulados desde el DOM/JavaScript; calcular y validar los totales obligatoriamente en el backend.
- Respetar presentaciones y equivalencias configuradas para cada medicamento.
- No asumir que todos los medicamentos pueden fraccionarse.
- No descontar existencias al abrir, editar o suspender una venta; descontar únicamente al confirmar la venta final.
- Proteger contra ventas duplicadas causadas por doble clic o reenvíos de formulario (patrón Post/Redirect/Get).
- Utilizar `@Transactional` en las operaciones que involucren varias escrituras (venta, detalles de venta, detalle-lote, inventario).
- Obtener el empleado asociado desde la sesión del usuario autenticado de forma automática.
- La reimpresión o consulta de tirilla/detalle no debe volver a registrar una venta ni descontar inventario nuevamente.
- No presentar la tirilla interna como factura electrónica fiscal si no existe integración legal configurada.

---

## 11. Compras, proveedores, lotes e inventario

- Los proveedores son registros comerciales independientes de las cuentas de usuario.
- Las compras deben conservar sus relaciones con proveedores y productos.
- Las entradas de inventario deben seguir las reglas del negocio y no duplicar existencias por una misma operación.
- Respetar los lotes, números de lote, fechas de vencimiento y trazabilidad.
- No permitir existencias negativas en inventario ni lotes.
- No eliminar compras históricas con detalles activos; si corresponde, registrar anulaciones controladas.
- Mantener consistencia matemática y transaccional entre compras, ventas y stock.

---

## 12. Git y protección de cambios

Antes de realizar cambios con Git:
- Revisar la rama actual (`git status`, `git branch`).
- Inspeccionar los cambios locales existentes antes de cualquier operación.
- Conservar los cambios no relacionados con la tarea.
- **Prohibido:** No ejecutar `git reset --hard`, `git clean` destructivo ni `git push --force` sin autorización expresa.
- No realizar commits ni pushes sin solicitud o aprobación expresa del usuario.

---

## 13. Pruebas y verificación

Verificar siempre las modificaciones en el entorno real disponible:
1. **Compilación:** Ejecutar `mvn compile` (o equivalente configurado con JDK 26/17 y codificación UTF-8).
2. **Pruebas automáticas:** Ejecutar `mvn test` y asegurar 0 fallos y 0 errores.
3. **Inicio de Spring Boot:** Validar que el contexto cargue y conecte exitosamente con MySQL (`vitanova_db`).
4. **Validación de vistas y rutas:** Comprobar que las rutas HTTP respondan correctamente y que Thymeleaf no arroje excepciones SpEL.
5. **Control de acceso:** Validar permisos de Administrador, Farmacéutico y Vendedor.
6. **Integridad de datos:** Comprobar que no se alteren registros ni estructuras ajenas a la tarea.
7. Nunca inventar resultados ni afirmar que algo funciona sin haberlo verificado.

---

## 14. Formato de entrega

Al concluir cualquier tarea en VitaNova PHARMA ERP, presentar el informe con:
1. **Cambios realizados:** Qué se corrigió o implementó.
2. **Causa identificada:** Origen comprobado del problema.
3. **Archivos modificados:** Rutas exactas y propósito del cambio.
4. **Base de datos:** Estado del esquema, consultas y migraciones pendientes (si aplican).
5. **Pruebas:** Cuáles se ejecutaron y sus resultados reales comprobables.
6. **Verificación manual:** Pasos claros para que el usuario verifique la funcionalidad en su navegador.
7. **Pendientes y limitaciones:** Aspectos abiertos o limitaciones detectadas.
8. **Git:** Sugerencia de mensaje de commit claro y ordenado.

---

## 15. Instrucción permanente

Antes de cada tarea, consultar este archivo `SKILL.md` y aplicar todas sus reglas.
Mantener una única fuente consolidada de instrucciones para VitaNova PHARMA ERP.
La prioridad absoluta es la **estabilidad, la seguridad, la integridad de los datos y el funcionamiento continuo del sistema**.
