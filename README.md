# Libro MPAZ — desarrollo RF-08

Sección DSY1105-005, Maipú. Basado en el proyecto del profesor y en
Requerimientos_DSY1105-005_v2 (08-10-2026), RF-08 y RN-02/03/04/08.

Integrante: Williams Tapia. Segundo integrante: **completar si corresponde**.
Repositorio GitHub privado: **completar con el enlace real**.

## Alcance real

Implementado RF-08: crear evaluación por curso/asignatura con nombre y fecha,
seleccionarla, cargar notas, validar y guardar, actualizar sin duplicar y auditar.
Se mantiene la estructura del profesor: pantalla → ViewModel → Repository → DAO → Room.
No se añadieron dependencias, backend, servicios externos, Hilt ni una nueva arquitectura.

Room persiste estudiantes de referencia, evaluaciones, notas y auditoría.
Los otros módulos de la base aún tienen TODO y datos en memoria; esta entrega de RF-08
NO representa la implementación completa de todos los RF ni garantiza el 100% de EP2.
Login básico y filtro de cargas se completaron como soporte mínimo de RF-01,
dependencia explícita de RF-08. Usuarios/cargas siguen siendo ficticios en MockData.
Se quitaron los botones que permitían entrar sin contraseña.

## Abrir y ejecutar

1. Descomprimir y abrir ESTA carpeta (contiene settings.gradle.kts) en Android Studio.
2. Usar JDK 17 para Gradle, tener instalado Android SDK 35 y sincronizar con Internet.
3. Ejecutar `app` en un emulador o teléfono API 24 o superior.
4. Ingresar como docente de Matemática, elegir 3° Básico A / Matemática y Calificaciones.
5. Crear una evaluación o seleccionar EV1 (Prueba unidad 1). Ingresar 5,8 para
   Estudiante 3A-03, Guardar notas y confirmar.
6. Salir y reabrir la app: seleccionar la evaluación y comprobar la nota.
7. Ingresar como Dirección y abrir Auditoría para comprobar el cambio.

| Perfil | Usuario | Clave |
| --- | --- | --- |
| Docente Matemática | docente.mat@libromp.cl | docente123 |
| Docente Lenguaje | docente.len@libromp.cl | docente123 |
| UTP | utp@libromp.cl | utp123 |
| Inspectoría | inspectoria@libromp.cl | inspectoria123 |
| Dirección | direccion@libromp.cl | direccion123 |

## Validaciones

- Nombre obligatorio. Fecha real en AAAA-MM-DD, incluidos años bisiestos.
- Sin repetir nombre y fecha dentro del mismo curso/asignatura (comparación sin distinguir mayúsculas).
- Notas entre 1,0 y 7,0; acepta coma o punto y como máximo un decimal.
- Un error impide guardar todo el lote. No hay guardados parciales.
- Los vacíos se ignoran: no crean una nota y tampoco borran una existente.
- El docente solo puede escribir en sus cargas; el estudiante debe pertenecer al curso.
- Confirmación antes de guardar. Cancelar no escribe.
- Nota y auditoría se guardan en la misma transacción; se conserva el ID al modificar.
- No hay eliminación de notas: RF-08 no la solicita.

El rango 1,0–7,0 se aplica según los criterios detallados del RF-08/RN-04;
el documento lo mantiene como supuesto SUP-09 pendiente de confirmación con CITT.
La unicidad del instrumento se interpreta por curso/asignatura, acorde al flujo.

## Recurso nativo y animación

`DatePickerDialog` de Android permite seleccionar una fecha de evaluación válida,
reduce errores de escritura y no requiere permisos, sensores ni conexión.
Se mantiene también el ingreso manual para probar las validaciones.
`animateContentSize` suaviza los cambios de tamaño del formulario al mostrar las notas.
Son las adiciones mínimas para los criterios G6 y G7 de la rúbrica; no venían resueltas
como ejemplo en la plantilla. Room sí estaba indicado en data/local y en RT-02.

## Pruebas y limitación de verificación

Se incluyen pruebas JUnit de notas/fechas y una prueba Android de Room que comprueba
creación, actualización sin duplicados, auditoría, rollback ante fallo y reapertura.
No se han ejecutado: el entorno de preparación tiene JDK 17, pero no dispone del
SDK Android ni de Gradle 8.7 descargado; la red bloqueó la descarga del wrapper.
La compilación y la ejecución visual quedan por verificar en Android Studio.
No se incluye APK ni se afirma que las pruebas hayan pasado.

En Git Bash, desde esta carpeta:

```bash
./gradlew testDebugUnitTest assembleDebug
./gradlew connectedDebugAndroidTest
```

El segundo comando necesita emulador o dispositivo. En PowerShell usar `./gradlew.bat`.

## Antes de entregar EP2

Completar los demás requerimientos comprometidos por el equipo, la persistencia que
les corresponde y los controles de sus operaciones. Completar integrantes y GitHub,
realizar commits reales y progresivos por integrante, revisar Trello y probar el proyecto
extraído desde un ZIP limpio. El ZIP de AVA debe contener este proyecto sin build/ ni
.gradle/, con el nombre `EP2_005_Tapia_<apellido2>.zip` (ajustar integrantes).
