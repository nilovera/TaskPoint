# Handoff para Codex — TaskPoint

Este archivo permite retomar el trabajo del proyecto en otra computadora sin tener que reconstruir el contexto desde cero. La fuente funcional y arquitectónica más completa sigue siendo [`Contexto/PROJECT_CONTEXT (1).md`](Contexto/PROJECT_CONTEXT%20(1).md).

## Prompt listo para pegar en Codex

```text
Estamos continuando TaskPoint, una app Android académica de organización de tareas por rutina semanal. Trabajá como agente de implementación: analizá el repositorio antes de cambiarlo, implementá la feature o corrección que te pida, verificá que compile y explicá con precisión qué cambiaste.

El repositorio Android real está en la carpeta anidada `TaskPoint/TaskPoint`; tomala como raíz. Empezá leyendo `CODEX_HANDOFF.md`, `Contexto/PROJECT_CONTEXT (1).md` y `Contexto/APPS1_SEGUNDA_PARTE_AGENT_NOTES.md`. Después revisá `git status --short` y preservá cualquier cambio ajeno.

Mantené Kotlin + Jetpack Compose + Material 3 y respetá la arquitectura existente. No hagas refactors masivos ni migres a Room/Retrofit salvo que yo lo pida explícitamente o sea indispensable para la feature. Usá cambios pequeños, coherentes y verificables. No reviertas ni borres trabajo existente; especialmente ignorá los cambios de `.idea/` si no son parte de la tarea.

Antes de dar por terminada una implementación, ejecutá `./gradlew.bat assembleDebug` desde la raíz Android y, si corresponde, validá el flujo manual afectado. Si algo está bloqueado, explicá la causa y qué necesitás, pero no inventes requisitos.

Reglas funcionales que no se pueden romper:
- Las tareas se asocian a una rutina y su día/horario debe ser compatible con esa rutina.
- Rutina: nombre, ícono, dirección, días, horario de inicio/fin y descripción; ícono y descripción son obligatorios.
- La UI no muestra estado pendiente/completada para las tareas.
- No usar GPS, geofencing ni Google Maps: las ubicaciones y comercios son datos sandbox.
- Conservá modo oscuro, textos escalables, `contentDescription` donde corresponda y la coherencia de navegación/UI.
- La UI Compose no debe acceder a fuentes de datos directamente. Conservá el flujo UI → ViewModel → Repository; las validaciones importantes deben quedar fuera de los composables.

Para cualquier pedido nuevo: primero indicá brevemente qué entendiste, inspeccioná sólo los archivos relevantes, implementá el cambio, compilá y entregá un resumen con archivos modificados y verificación realizada.
```

## Foto actual del proyecto

- Android nativo: Kotlin, Jetpack Compose y Material 3.
- Paquete actual: `com.example.apk_mock`. Aunque el nombre sea histórico, no renombrarlo como efecto colateral de una feature.
- Mínimo API 26; compilación/target API 36.
- Navegación Compose en `app/src/main/java/com/example/apk_mock/ui/navigation/AppNavigation.kt`.
- Persistencia actual basada en JSON local, mediante `Json*Repository` y `JsonDataSource`; aún **no** hay Room.
- Datos iniciales: `app/src/main/assets/seed/`.
- Datos sandbox de categorías/ofertas/comercios: `app/src/main/assets/sandbox/`.
- Fotos de tareas: flujo CameraX y `TaskPhotoStorage`.
- Tema claro/oscuro: `ui/theme/`.
- Build base comprobado el 2026-06-20: `./gradlew.bat assembleDebug` finaliza correctamente.

## Flujos ya presentes

- Onboarding, registro, inicio de sesión, recuperación/cambio de contraseña y perfil.
- Home, listado/creación/detalle/edición/borrado de rutinas, con filtro por día.
- Listado/creación/detalle/edición/borrado de tareas, con categorías, horarios derivados de la rutina y foto opcional por cámara.
- Ofertas sandbox relacionadas con categorías.

Antes de afirmar que alguno de esos flujos está completo, comprobalo en el código y en ejecución: son implementaciones en evolución, no una garantía de ausencia de bugs.

## Guía de trabajo segura

1. Ubicarse en la raíz real:

   ```powershell
   cd TaskPoint\TaskPoint
   ```

2. Leer los dos documentos de `Contexto/` y este archivo antes de plantear cambios arquitectónicos.
3. Ejecutar `git status --short` antes y después. No modificar ni descartar cambios que no pertenezcan al pedido.
4. Buscar primero el flujo que usa la pantalla, el ViewModel y el repositorio afectados; evitar duplicar estado o lógica.
5. Implementar de forma acotada, conservando el estilo y los componentes reutilizables existentes.
6. Compilar al finalizar:

   ```powershell
   .\gradlew.bat assembleDebug
   ```

7. Informar: comportamiento entregado, archivos cambiados, validación ejecutada y cualquier limitación concreta.

## Prioridades arquitectónicas

El proyecto hoy tiene una arquitectura MVVM ligera, con ViewModels que trabajan contra repositorios JSON. Al sumar una feature, mantener esa separación y mejorarla de manera incremental:

```text
Compose UI → ViewModel (UiState/eventos) → Repository → JSON/almacenamiento local
```

No poner lectura/escritura de JSON, archivos, validaciones extensas ni trabajo pesado en un composable. Cuando haya una regla de negocio real (por ejemplo validar día y horario de la tarea), dejala centralizada y testeable en ViewModel, repositorio o un use case si se incorpora uno con sentido.

Una futura migración a Room/offline-first está documentada como objetivo en `PROJECT_CONTEXT (1).md`, pero no conviene mezclarla con arreglos visuales o features pequeñas. Debe hacerse como tarea dedicada, con plan, mappers y pruebas de migración/lectura.

## Reglas de producto y coherencia

- La app organiza tareas según una **rutina semanal declarada**, no según seguimiento de ubicación en tiempo real.
- `Place` es un concepto del dominio esperado por la documentación, pero la implementación actual modela la dirección en la rutina. No introducir una entidad nueva sin revisar impacto en datos, navegación y documentación.
- Los comercios/ofertas son demostrativos y deben salir de los JSON sandbox, nunca quedar hardcodeados en la UI.
- Mantener la coherencia con Figma y documentación; no agregar secciones o estados nuevos sólo porque parecen útiles.
- Usar los colores y tokens existentes en `ui/theme`; evitar colores hardcodeados si ya hay una alternativa de tema.
- Errores de formularios, cámara, permisos o datos deben dar feedback visible, sin crashes silenciosos.

## Estado de Git al momento de este handoff

Al crear este archivo había cambios sin confirmar:

```text
M  .idea/misc.xml
?? Contexto/APPS1_SEGUNDA_PARTE_AGENT_NOTES.md
```

Son cambios existentes del usuario/IDE: no se deben revertir ni incluir automáticamente en un commit ajeno. `CODEX_HANDOFF.md` también será nuevo hasta que se agregue al repositorio.

## Cómo usarlo en la otra computadora

Copiá o cloná el repositorio incluyendo este archivo. Abrí Codex desde la carpeta contenedora o desde la raíz Android y pegá el prompt de arriba. Después agregá el pedido concreto, por ejemplo:

> Leé `CODEX_HANDOFF.md`. Corregí el flujo de edición de tareas: revisá la navegación, los estados y el repositorio, aplicá un arreglo mínimo y verificá con `assembleDebug`.

Ese patrón le da al nuevo Codex contexto suficiente, pero mantiene cada tarea específica y controlable. Pequeño ritual de continuidad; gran ahorro de volver a explicarle todo al robot nuevo.
