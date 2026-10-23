# Libro MPAZ

**Prototipo académico de uso interno** · DSY1105 Desarrollo de Aplicaciones Móviles · Sección DSY1105-005 (Maipú) · Evaluación Parcial 2

Libro de clases digital para la Escuela Escritora Marcela Paz. Reemplaza el registro en papel (asistencia, contenidos/OA, calificaciones y hoja de vida) por una app Android con vistas distintas según el perfil del funcionario. Todos los datos son ficticios. No tiene validez oficial ante el MINEDUC ni el sostenedor.

## Integrantes

- Williams Tapia
- Francisco Ancamil
- Jonelvi Castro
- Felipe Arce

**Repositorio GitHub (privado):https://github.com/FelipeArce07/LibroMpaz_RF08.git

## Tecnologías

Kotlin 2.0 · Jetpack Compose · Material Design 3 · arquitectura MVVM · Room (SQLite) · Navigation Compose · Gradle 8.7 / AGP 8.5.2.

## Requerimientos implementados

Basado en *Requerimientos_DSY1105-005 v2* (08-10-2026).

| Requerimiento | Estado |
| --- | --- |
| RF-01 Autenticación y cursos asignados | Implementado (básico): login con usuarios ficticios; "Mis cursos" muestra solo las cargas del docente. |
| RF-02 Control de acceso por rol | Parcial: cada perfil tiene su propia navegación; los permisos se verifican en el ViewModel solo para calificaciones. |
| **RF-08 Calificaciones por instrumento y fecha** | **Implementado con Room** (detalle más abajo). |
| RF-13 Registro de auditoría | Implementado para las acciones de RF-08. Dirección lo consulta en modo solo lectura. |
| RF-03 a RF-07, RF-09 a RF-12 | No implementados. Sus pantallas existen con datos de ejemplo en memoria (`MockData`) y no guardan cambios. |

### Qué hace RF-08

- Crear una evaluación por curso y asignatura, con nombre y fecha.
- Seleccionarla y cargar una nota por estudiante.
- Validar y guardar el lote completo con confirmación previa.
- Actualizar una nota sin duplicarla (se conserva el ID).
- Registrar cada creación o modificación en la auditoría.

## Cómo ejecutar

**Requisitos:** Android Studio reciente, JDK 17, Android SDK 35 e Internet para la primera sincronización.

1. Clonar o descomprimir el proyecto y abrir la carpeta que contiene `settings.gradle.kts`.
2. Esperar el *Gradle Sync*. No actualizar Gradle ni el plugin de Android.
3. Ejecutar el módulo `app` en un emulador o teléfono con Android 7.0 (API 24) o superior.

### Usuarios de prueba

| Perfil | Usuario | Clave |
| --- | --- | --- |
| Docente Matemática | docente.mat@libromp.cl | docente123 |
| Docente Lenguaje | docente.len@libromp.cl | docente123 |
| UTP | utp@libromp.cl | utp123 |
| Inspectoría | inspectoria@libromp.cl | inspectoria123 |
| Dirección | direccion@libromp.cl | direccion123 |

### Recorrido sugerido para probar RF-08

1. Ingresar como Docente Matemática, elegir *3° Básico A / Matemática* y abrir **Calificaciones**.
2. Crear una evaluación o seleccionar EV1.
3. Probar valores inválidos (`8`, `abc`, `5,55`) y una fecha inválida (`2026-02-30`): no deben permitir guardar.
4. Ingresar `5,8` para *Estudiante 3A-03*, guardar y confirmar.
5. Cerrar la app por completo, abrirla de nuevo y comprobar que la nota sigue ahí.
6. Ingresar como Dirección → **Auditoría** y comprobar el registro del cambio.

## Arquitectura

Flujo: **Pantalla → ViewModel → Repository → DAO → Room.**

| Capa | Archivo principal | Responsabilidad |
| --- | --- | --- |
| UI | `ui/screen/docente/CalificacionesScreen.kt` | Mostrar estado y enviar eventos; sin reglas de negocio. |
| ViewModel | `viewmodel/LibroMpazViewModel.kt` | Permisos, validaciones y coordinación (StateFlow). |
| Repository | `data/CalificacionesRepository.kt` | Guardado en transacción y lectura como Flow. |
| DAO / Entities | `data/local/` | Consultas SQL y tablas. |
| Validaciones | `util/ValidacionesCalificaciones.kt` | Funciones puras, probadas con JUnit. |

Room persiste estudiantes de referencia, evaluaciones, notas y auditoría. Los usuarios y las cargas docentes siguen en `MockData`.

## Reglas y validaciones (RF-08)

- Nombre de evaluación obligatorio y fecha real en formato AAAA-MM-DD, incluidos años bisiestos.
- No se repite el mismo nombre y fecha dentro del mismo curso y asignatura (sin distinguir mayúsculas).
- Notas entre 1,0 y 7,0, con coma o punto y como máximo un decimal.
- Un error impide guardar todo el lote: no hay guardados parciales.
- Los campos vacíos se ignoran: no crean notas ni borran las existentes.
- El docente solo escribe en sus propias cargas, y el estudiante debe pertenecer al curso.
- Confirmación antes de guardar; cancelar no escribe nada.
- La nota y su auditoría se guardan en la misma transacción.
- La auditoría solo admite inserción y lectura: no se edita ni se borra.

## Recurso nativo y animación

- **Recurso nativo:** `DatePickerDialog` de Android para elegir la fecha de la evaluación. [Completar: confirmar con el profesor si se acepta como recurso nativo.] Reduce errores de escritura de la fecha, no requiere permisos, sensores ni conexión y respeta las restricciones del caso. Se mantiene el ingreso manual de fecha para probar las validaciones.
- **Animación:** `animateContentSize` suaviza el cambio de tamaño del formulario al mostrar las notas.

## Restricciones del caso

- Sin GPS, cámara, micrófono ni huellas: el manifest no declara esos permisos.
- Sin notificaciones, correos, SMS ni WhatsApp.
- Sin acceso para estudiantes ni apoderados: solo existen los cuatro perfiles de funcionarios.
- Datos 100 % ficticios (Ley 19.628 y Ley 21.719).

## Supuestos pendientes de confirmación con el CITT

- **SUP-09:** la escala 1,0 a 7,0 se valida dentro de la app.
- La unicidad de la evaluación se interpreta por curso y asignatura.

## Pruebas

- **JUnit:** `ValidacionesCalificacionesTest` (notas y fechas).
- **Android (Room):** `CalificacionesRoomTest` (creación, actualización sin duplicados, auditoría, rollback y reapertura).

```bash
./gradlew testDebugUnitTest assembleDebug
./gradlew connectedDebugAndroidTest   # requiere emulador o dispositivo
```

En PowerShell usar `./gradlew.bat`.

| Verificación | Resultado |
| --- | --- |
| Compila en Android Studio | [completar: sí / no] |
| Pruebas JUnit | [completar] |
| Prueba Android de Room | [completar] |
| Recorrido manual de RF-08 en emulador | [completar] |

## Limitaciones conocidas

- Solo RF-08 usa Room; los demás módulos usan datos en memoria y no persisten.
- No hay backend ni consumo de API REST (Retrofit está configurado, pero no se usa).
- No se incluye APK firmado. [Completar si el profesor lo exige.]