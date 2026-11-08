# Sistema de Movilidad Compartida

Trabajo Práctico Obligatorio de **Programación Orientada a Objetos** – Propuesta C
Universidad Argentina de la Empresa · Docente: María Ángela León

## Integrantes

| Integrante | Legajo |
|---|---|
| Astudillo Solis, Valentina | 1211931 |
| Pacheco, Maitena Alumine | 1216103 |
| Romero Chacon, Marcos Ignacio | 1219650 |

Repositorio: <https://github.com/Mromeroch/POO_TPO.git>

## Descripción del sistema

Aplicación de consola en Java que simula un servicio público de **bicicletas y monopatines compartidos** de un municipio. Los vehículos se guardan en estaciones, se retiran y se devuelven, y cada viaje se cobra de una cuenta virtual prepaga.

Hay cuatro roles, cada uno con su menú (sin autenticación):

| Rol | Qué puede hacer |
|---|---|
| **Usuario** | Registrarse, asociar medios de pago (tarjeta o billetera virtual), recargar saldo, suscribirse al plan mensual, retirar y devolver vehículos, registrar puntos del recorrido, cancelar un retiro, ver su historial y reportar fallas. |
| **Operador** | Ver la ocupación de las estaciones y sus alertas, redistribuir vehículos, reportar fallas, generar órdenes de mantenimiento, asignar técnicos, registrar notas de ajuste y detectar vehículos perdidos. |
| **Técnico** | Consultar sus órdenes, registrar tareas y repuestos y cerrar órdenes de mantenimiento. |
| **Administrador** | Emitir reportes: viajes por usuario (con rango de fechas opcional), uso por estación y vehículos con más fallas. |

### Reglas de negocio principales

- **Vehículos:** bicicleta común, bicicleta eléctrica y monopatín eléctrico. Estados: disponible, en uso, en reparación, batería baja y fuera de servicio. Un eléctrico con batería menor al 15 % pasa a batería baja y no se ofrece.
- **Retiro:** el usuario debe estar habilitado, no tener otro viaje abierto y alcanzar el saldo mínimo (desbloqueo + 30 minutos de uso: $800 bicicleta común, $1.650 bicicleta eléctrica, $1.800 monopatín).
- **Tarifas de referencia:** bicicleta común $200 + $20/min, bicicleta eléctrica $300 + $45/min, monopatín $300 + $50/min. Los minutos posteriores al 120 llevan un 50 % de recargo.
- **Promociones** (no se acumulan, se cobra la más favorable): primer viaje gratis hasta 15 minutos y plan mensual (20 viajes de hasta 30 minutos, 30 días de duración, precio de referencia $6.000).
- **Cancelación:** un retiro cancelado dentro de los 2 minutos no tiene cargo.
- **Estaciones:** capacidad fija. Si está llena, el usuario tiene 10 minutos sin cargo para llegar a otra. Una ocupación menor al 20 % o mayor al 90 % genera una alerta de redistribución.
- **Saldo negativo:** el usuario queda inhabilitado hasta regularizarlo.
- **Vehículo perdido:** a las 24 horas sin devolución el viaje pasa a PERDIDO y se cobra un cargo fijo de $20.000.
- **Mantenimiento:** 3 reportes de falla en 30 días pasan el vehículo a mantenimiento obligatorio. Una orden no se cierra sin técnico, fecha y tareas.
- **Viajes cerrados:** no se modifican; las correcciones se hacen con notas de ajuste.

Los supuestos adoptados ante lo que el enunciado deja abierto (S01 a S25) están documentados en el informe de la carpeta `docs/`.

## Diseño

- **Jerarquías de herencia con sobreescritura:** `Vehiculo` → `BicicletaComun` / `VehiculoElectrico` → `BicicletaElectrica` / `MonopatinElectrico`; `Empleado` → `Operador` / `Tecnico` / `Administrador`; `TarifaBase` → tarifas por tipo de vehículo; `Promocion` → `PromocionPrimerViaje` / `PromocionPlanMensual`.
- **Principios SOLID aplicados:** SRP, OCP y DIP.
- **Patrones GRASP aplicados:** Experto, Creador, Controlador y Polimorfismo.
- **Colecciones y orden:** `Viaje` implementa `Comparable` (orden por fecha de inicio) y `ComparadorFallasVehiculo` implementa `Comparator` (más fallas primero).
- **Excepciones:** jerarquía propia con raíz `MovilidadException` (verificada) para las reglas de negocio.
- **Persistencia:** archivos CSV separados por `;` en la carpeta `data/`.
- **Reloj inyectable:** la interfaz `Reloj` permite probar los límites temporales con `RelojPrueba`.

## Requisitos

- **Java 17 o superior** (JDK).
- **JUnit 5** (solo para ejecutar las pruebas).
- Opcional: un IDE como IntelliJ IDEA, Eclipse o NetBeans.

Verificá la versión instalada con:

```
java -version
javac -version
```

## Estructura del proyecto

```
/docs                  informe, plan de pruebas funcionales y diagramas UML
/src/movilidad         código fuente
    Main.java          punto de entrada
    Sistema.java       raíz de composición (crea y conecta los objetos)
    DatosIniciales.java  datos de prueba de la primera ejecución
    /controladores     casos de uso (GRASP Controlador)
    /dominio           entidades y reglas del dominio
    /excepciones       excepciones propias
    /interfaz          interfaz de consola con menús por rol
    /persistencia      interfaces de repositorio y su implementación en memoria y en archivos
    /servicios         tarifas, promociones, pagos simulados, control de viajes y reportes
    /util              dinero, reloj y lectura/escritura de CSV
/test/movilidad        pruebas unitarias (JUnit 5)
README.md
```

## Instrucciones de instalación y ejecución

### Opción A: desde un IDE

1. Clonar el repositorio: `git clone https://github.com/Mromeroch/POO_TPO.git`
2. Abrir la carpeta como proyecto Java, con `src` como carpeta de fuentes y `test` como carpeta de pruebas.
3. Configurar el JDK 17 o superior y agregar JUnit 5 a las librerías del proyecto.
4. Ejecutar la clase `movilidad.Main`.

Si el IDE no usa la carpeta del proyecto como directorio de trabajo, los datos se guardan en `data/` relativo a donde se ejecute. Se puede indicar otra carpeta como argumento (ver más abajo).

### Opción B: desde la terminal

**Linux / macOS**

```bash
git clone https://github.com/Mromeroch/POO_TPO.git
cd POO_TPO
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out movilidad.Main
```

**Windows (CMD)**

```bat
git clone https://github.com/Mromeroch/POO_TPO.git
cd POO_TPO
mkdir out
dir /s /b src\*.java > fuentes.txt
javac -encoding UTF-8 -d out @fuentes.txt
java -cp out movilidad.Main
```

Para guardar los datos en otra carpeta, pasarla como argumento:

```
java -cp out movilidad.Main mis_datos
```

Si no se indica nada, se usa la carpeta `data/`.

### Cómo se usa

Al iniciar aparece el menú principal. Se elige un rol y se opera escribiendo el número de la opción:

```
MENÚ PRINCIPAL
 1. Usuario
 2. Operador
 3. Técnico
 4. Administrador
 0. Salir
```

- Como **usuario**, la opción 2 permite ingresar con un documento ya registrado.
- Como **operador** y **técnico**, el sistema pide el legajo.
- Al devolver un vehículo que no tiene puntos de recorrido, se pide la distancia en kilómetros.
- Los datos se guardan automáticamente después de cada operación que los modifica y se cargan al iniciar, por lo que se conservan entre ejecuciones.
- Para **reiniciar** el sistema a los datos de prueba, cerrar el programa y borrar la carpeta `data/`.

## Datos de prueba

La primera vez que se ejecuta (carpeta `data/` vacía o inexistente) se cargan estos datos:

| Tipo | Datos |
|---|---|
| **Usuarios** | Juan Pérez, documento `30111222`, saldo $20.000 y una tarjeta asociada · Ana López, documento `28999888`, saldo $0 y una billetera virtual asociada |
| **Estaciones** | `E01` Plaza Central (capacidad 10): B-001, B-002, E-001, M-001 · `E02` Terminal de Ómnibus (capacidad 6): B-003, E-002, M-002 · `E03` Parque Norte (capacidad 8): M-003, B-004 |
| **Operador** | legajo `OP-01` |
| **Técnico** | legajo `TEC-01` |
| **Administrador** | legajo `ADM-01` |

Los códigos de vehículo empiezan con `B-` (bicicleta común), `E-` (bicicleta eléctrica) y `M-` (monopatín eléctrico).

## Pruebas

### Pruebas unitarias

Cubren la lógica de negocio con un `Escenario` en memoria y un reloj controlable: tarifas, promociones, viajes, cuentas, vehículos, estaciones, mantenimiento, redistribución, reportes, control de viajes perdidos y persistencia en archivos.

- **Desde el IDE:** ejecutar la carpeta `test` como pruebas JUnit.
- **Desde la terminal:** descargar `junit-platform-console-standalone` (JUnit 5) y ejecutar, con el jar en la carpeta del proyecto:

```bash
# Linux / macOS
mkdir -p out-test
javac -encoding UTF-8 -cp out:junit-platform-console-standalone.jar -d out-test $(find test -name "*.java")
java -jar junit-platform-console-standalone.jar -cp out:out-test --scan-classpath
```

En Windows se usa `;` en lugar de `:` como separador del classpath y la lista de fuentes se genera como en la compilación.

### Plan de pruebas funcionales

Los casos de prueba con datos de entrada, resultado esperado y resultado obtenido para los casos de uso principales están en `docs/PLAN_DE_PRUEBAS_FUNCIONALES.md`.

## Documentación

La carpeta `docs/` contiene el informe de diseño de la Etapa 1 (análisis, casos de uso, diagramas UML, patrones SOLID y GRASP, supuestos), el plan de pruebas funcionales y el informe final.

## Versiones entregadas

| Etapa | Contenido | Tag |
|---|---|---|
| Etapa 1 | Modelado y aplicación de patrones | `etapa-1` |
| Etapa 2 | Codificación, puesta a punto y testeo | `etapa-2` |