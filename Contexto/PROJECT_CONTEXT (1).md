# PROJECT_CONTEXT.md — TaskPoint

## 1. Identidad del proyecto

**Nombre del proyecto:** TaskPoint  
**Materia:** Desarrollo de Aplicaciones I / Aplicaciones Móviles  
**Tecnología principal:** Android nativo con Kotlin + Jetpack Compose  
**Enfoque:** App mobile con arquitectura moderna, persistencia local/remota, modo offline, Material 3 y buenas prácticas de accesibilidad.

TaskPoint es una aplicación Android pensada para organizar tareas en función de la rutina semanal declarada por el usuario. El usuario primero carga sus rutinas, lugares, días y horarios. Luego crea tareas asociadas a una rutina, un día y un horario válido.

La app no se basa en GPS ni geofencing real. Trabaja con rutinas declaradas por el usuario y ubicaciones sandbox. El objetivo funcional es recordarle al usuario qué debe llevar o hacer según el lugar y momento de su día.

---

## 2. Concepto funcional de TaskPoint

La lógica principal de TaskPoint es:

1. El usuario crea un Lugar (Place) con nombre, dirección y coordenadas.
2. El usuario crea una rutina semanal asociada a ese Lugar.
3. Cada rutina tiene lugar, días, horario, ícono y descripción.
4. El usuario crea tareas asociadas a una rutina existente.
5. La tarea debe tener un día y horario compatible con la rutina.
6. La app muestra tareas del día agrupadas por rutina en el Home.
7. La app puede mostrar sugerencias comerciales sandbox según la categoría de la tarea y la ubicación del Place asociado a la rutina.

Ejemplo:

- Lugar: Av. Santa Fe 3000, CABA
- Rutina: Trabajo presencial — Lunes, Mié, Vie — 09:00 a 17:00

Tarea:

- Título: Comprar comida
- Categoría: Supermercado
- Rutina asociada: Trabajo presencial
- Día: lunes
- Hora: 17:00
- Foto: opcional desde cámara o galería

La app puede mostrar:

> Hoy vas al Trabajo a las 09:00. No te olvides: llevar notebook.

---

## 3. Consigna del TP que se debe respetar

El proyecto debe cumplir con la consigna del Trabajo Práctico Integrador de Desarrollo de Aplicaciones I.

### Hito 1 — H1 obligatorio

Debe incluir:

- Figma.
- Flujo de pantallas.
- Repositorio inicializado.
- Tablero de seguimiento.
- Al menos 2 casos de uso.
- APK demo, aunque puede estar mockeado.
- Diagrama inicial de arquitectura a alto nivel.
- Descripción de tecnologías elegidas.

### Hito 2 — H2 obligatorio

Debe incluir:

- Feature set completo.
- Pruebas.
- Métricas.
- APK Release Candidate.
- Documentación final.
- Defensa técnica.

---

## 4. Requisitos funcionales obligatorios

TaskPoint debe contemplar:

1. Onboarding inicial al momento de la instalación.
2. Al menos 3 flujos de pantallas distintos.
3. Al menos un CRUD completo del dominio principal.
4. Autenticación mediante email/contraseña y/o autenticación federada.
5. Modo offline con funcionalidad mínima.
6. Al menos un listado compuesto por Card Views.
7. Uso de al menos un sensor o dispositivo de captura.
8. Tamaños de fuente escalables.
9. `contentDescription` en elementos visuales relevantes.
10. Tema oscuro.
11. Internacionalización opcional, solo si se implementa correctamente.

En TaskPoint, el dispositivo de captura será principalmente la **cámara**, y también se contempla la **galería** para adjuntar una imagen a una tarea.

---

## 5. Requisitos no funcionales

TaskPoint debe contemplar:

- Cold start menor a 2.5 segundos en dispositivo de referencia.
- Scroll fluido mayor a 54 fps.
- Buen manejo de errores de conectividad.
- Posible cola offline, si se implementa.
- Justificación del mínimo API Level según público objetivo.

### Público objetivo sugerido

TaskPoint puede orientarse a usuarios con rutinas semanales organizadas:

- Estudiantes.
- Trabajadores presenciales o híbridos.
- Personas que combinan trabajo, facultad, gimnasio, trámites y actividades personales.
- Familias que organizan rutinas de otros integrantes.

No se debe plantear como una app exclusivamente para adultos mayores, aunque sí debe contemplar accesibilidad.

---

## 6. Requisitos arquitectónicos

La arquitectura base debe ser:

```text
Composable / Screen
        ↓
ViewModel
        ↓
UseCase
        ↓
Repository
        ↓
DataSource local / remoto
```

Se deben respetar:

- MVVM.
- Repository Pattern.
- Use Case Pattern.
- DataSource Pattern.
- DTO / Entity / Domain Model.
- Clean Architecture adaptada.
- Dependency Injection o contenedor de dependencias.
- Navigation Pattern.
- Single Source of Truth.
- Offline First.
- UI declarativa.
- State Hoisting.
- Unidirectional Data Flow.
- UI State Pattern.
- StateFlow.
- Eventos de UI.
- One-shot events.
- Side Effects controlados.

---

## 7. Separación de responsabilidades

### UI / Composables

Responsabilidades:

- Renderizar estado.
- Emitir eventos.
- Mostrar feedback visual.
- Usar Material 3.
- Ser rápida, predecible y sin efectos secundarios pesados.

No debe:

- Acceder directamente a Room.
- Acceder directamente a Retrofit.
- Validar reglas complejas de negocio.
- Guardar datos directamente.
- Tener lógica de sincronización.
- Contener funciones gigantes tipo God Composable.

### Screen

Responsabilidades:

- Conectarse al ViewModel.
- Observar `StateFlow`.
- Colectar effects con `LaunchedEffect`.
- Pasar estado y callbacks al Content.
- Manejar navegación mediante callbacks específicos.

### Content

Responsabilidades:

- Ser stateless siempre que sea posible.
- Recibir `uiState`.
- Recibir callbacks como `onEvent`, `onBack`, `onSaveClick`.
- Renderizar la pantalla.

### ViewModel

Responsabilidades:

- Mantener estado de UI.
- Procesar eventos de pantalla.
- Invocar UseCases.
- Exponer `StateFlow`.
- Emitir effects de navegación, snackbar o mensajes temporales.
- Usar `viewModelScope` para operaciones asincrónicas.

No debe:

- Tener SQL.
- Tener llamadas HTTP directas.
- Manipular archivos directamente.
- Contener validaciones complejas de negocio.
- Conocer detalles de Room o Retrofit.
- Volverse un ViewModel obeso.

### UseCase

Responsabilidades:

- Validar reglas de aplicación.
- Validar reglas de negocio.
- Coordinar operaciones específicas del dominio.
- Ser testeable sin Android ni Compose.

En TaskPoint, las validaciones de crear rutina y crear tarea deben estar en el UseCase.

### Repository

Responsabilidades:

- Abstraer origen de datos.
- Decidir si usa Room, backend, JSON/assets o memoria.
- Implementar política offline first.
- Guardar primero localmente.
- Intentar sincronizar con backend.
- Actualizar `syncStatus`.

### DataSource

Responsabilidades:

- Hablar con una fuente concreta.
- Local DataSource: Room, DAO, assets JSON.
- Remote DataSource: API REST con Retrofit/Gson o similar.
- Camera/Gallery DataSource si se separa el acceso a imagen.

---

## 8. Patrones obligatorios a respetar

### UI declarativa

La pantalla es una función del estado:

```text
UI = f(estado)
```

No se modifica la UI manualmente. Cambia el estado y Compose recompone.

### Composition Pattern

La UI debe dividirse en componentes pequeños, reutilizables y claros.

Ejemplos:

- `TaskCard`
- `RoutineCard`
- `OfferCard`
- `TaskForm`
- `RoutineForm`
- `EmptyState`
- `ErrorMessage`
- `LoadingIndicator`

### Screen / Content

Cada pantalla importante debe separar:

```text
CrearTareaScreen.kt    → conectado al ViewModel
CrearTareaContent.kt   → stateless, recibe estado y eventos
```

### Stateful / Stateless Composables

Los componentes reutilizables deben ser stateless cuando sea posible.

### State Hoisting

El estado se eleva al nivel que corresponde. Los campos reciben:

```kotlin
value: String
onValueChange: (String) -> Unit
```

### Unidirectional Data Flow

El flujo correcto es:

```text
State baja hacia la UI
Event sube hacia el ViewModel
ViewModel procesa
UseCase valida
Repository guarda/sincroniza
Nuevo State baja hacia la UI
```

### UI State Pattern

Cada pantalla importante debe tener un `UiState`.

Ejemplo:

```kotlin
data class CrearTareaUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categorias: List<Category> = emptyList(),
    val rutinas: List<Routine> = emptyList(),
    val rutinaSeleccionada: Routine? = null,
    val diasDisponibles: List<DayOfWeek> = emptyList(),
    val horariosDisponibles: List<String> = emptyList(),
    val fotoUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Eventos de UI

Ejemplo:

```kotlin
sealed interface CrearTareaEvent {
    data class TituloChanged(val value: String) : CrearTareaEvent
    data class DescripcionChanged(val value: String) : CrearTareaEvent
    data class RutinaSeleccionada(val routineId: String) : CrearTareaEvent
    data class DiaSeleccionado(val dia: DayOfWeek) : CrearTareaEvent
    data class HoraSeleccionada(val hora: String) : CrearTareaEvent
    data class FotoSeleccionada(val uri: String) : CrearTareaEvent
    data object GuardarClick : CrearTareaEvent
}
```

### One-shot events / Effects

Para navegación y snackbar:

```kotlin
sealed interface CrearTareaEffect {
    data object NavigateBackToTasks : CrearTareaEffect
    data class ShowSnackbar(val message: String) : CrearTareaEffect
}
```

### Side Effects controlados

Usar `LaunchedEffect` para:

- Colectar effects.
- Lanzar navegación.
- Mostrar snackbar.
- Pedir permisos o disparar acciones controladas.

No llamar a funciones de carga directamente en el cuerpo del composable si eso puede repetirse por recomposición.

---

## 9. Offline First y sincronización

TaskPoint debe manejar offline de forma defendible, simple y consistente.

### Regla central

La UI debe observar Room como fuente local de verdad siempre que sea posible.

```text
Backend API → Repository → Room → ViewModel → UI
```

Para operaciones de escritura:

```text
UI → ViewModel → UseCase → Repository → Room → Backend API
```

### Política acordada

1. El usuario crea, edita o elimina una tarea/rutina.
2. El Repository guarda primero en Room.
3. Si hay conexión, intenta sincronizar con backend.
4. Si sincroniza correctamente, actualiza `syncStatus = SYNCED`.
5. Si falla la red, deja el dato con estado pendiente.
6. Cuando vuelve la conexión, el Repository intenta sincronizar pendientes.

### Estados de sincronización

```kotlin
enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}
```

### Importante

No prometer sincronización compleja con resolución avanzada de conflictos. Documentar como sincronización básica offline first, suficiente para el TP.

---

## 10. Persistencia local y backend

### Room

Room debe usarse para datos estructurados:

- Tareas.
- Rutinas.
- Lugares (Place).
- Categorías.
- Comercios sandbox.
- Ofertas sandbox, si se precargan.

### Backend API

Se usa para sincronizar datos principales:

- Tareas.
- Rutinas.
- Usuario.
- Places, si aplica.

Puede estar mockeado o ser una API simple, según el estado del proyecto.

### JSON sandbox

Los datos comerciales no deben estar hardcodeados en la UI.

Archivos recomendados:

```text
app/src/main/assets/sandbox/stores.json
app/src/main/assets/sandbox/offers.json
app/src/main/assets/sandbox/categories.json
app/src/main/assets/sandbox/demo_places.json
```

Flujo recomendado:

```text
JSON sandbox
        ↓
SandboxSeeder / SandboxDataSource
        ↓
Room
        ↓
Repository
        ↓
ViewModel
        ↓
UI Compose
```

---

## 11. Entidades principales

### User

```text
User
- id
- nombre
- email
- password / authProvider
- createdAt
```

### Place / Lugar

Place es una entidad propia e independiente. Representa una ubicación frecuente del usuario. "Lugar" es el concepto funcional de negocio; `Place` es el nombre técnico de la entidad y la tabla en Room.

El usuario crea un Place al configurar una rutina. La dirección ingresada se transforma en coordenadas mediante un geocoder sandbox local (lista precargada en `demo_places.json`). Las coordenadas se usan luego para calcular distancias contra comercios sandbox.

```text
Place
- id
- userId
- nombre
- direccion
- latitud
- longitud
- descripcion
- syncStatus
```

Las coordenadas se obtienen mediante geocoding sandbox local, no GPS. Se guardan una sola vez al crear el Place y se reutilizan.

### Routine / Rutina

```text
Routine
- id
- userId
- placeId
- titulo
- descripcion        ← obligatorio
- icono              ← obligatorio
- diasSemana         // ["MONDAY", "WEDNESDAY", "FRIDAY"]
- horaInicio
- horaFin
- createdAt
- updatedAt
- syncStatus
```

El ícono y la descripción son obligatorios según el diseño acordado en Figma.

### Task / Tarea

```text
Task
- id
- userId
- routineId
- categoryId
- titulo
- descripcion
- diaSemana
- hora
- fotoUri
- notas
- createdAt
- updatedAt
- syncStatus
```

La tarea debe validar que el día y horario pertenezcan a la rutina asociada. Las tareas **no tienen estado visible** (sin pendiente/completado en UI).

### Category / Categoría

```text
Category
- id
- nombre
- descripcion
- code
- activaOfertas
```

### Store / Comercio sandbox

```text
Store
- id
- nombre
- categoryId
- direccion
- latitud
- longitud
- logoUrl
```

### Offer / Oferta sandbox

```text
Offer
- id
- storeId
- titulo
- descripcion
- fechaInicio
- fechaFin
- destacado
```

### TaskAttachment

Puede simplificarse y no crearse como entidad separada. Para el TP alcanza con `fotoUri` dentro de `Task`.

---

## 12. CRUD principal: Tareas

El CRUD principal de TaskPoint es **Tareas**.

### Crear tarea

Campos:

- Título (obligatorio).
- Descripción (opcional).
- Categoría (obligatorio).
- Rutina asociada (obligatorio).
- Día (obligatorio, dentro de los días de la rutina).
- Horario (obligatorio, dentro del rango de la rutina).
- Foto opcional desde cámara o galería.
- Notas opcionales.

Validaciones:

- El título no puede estar vacío.
- Debe seleccionar categoría.
- Debe seleccionar rutina.
- El día debe pertenecer a la rutina.
- El horario debe estar dentro del rango de la rutina.
- Si hay foto, debe guardarse/registrarse como URI válida.

### Ver tarea

Mostrar:

- Título.
- Descripción.
- Categoría.
- Rutina asociada.
- Lugar/dirección del Place de la rutina.
- Día.
- Horario.
- Foto si existe.
- Notas si existen.
- Ofertas sugeridas si la categoría aplica.

No mostrar "estado de tarea" — las tareas no tienen estado visible en UI.

### Editar tarea

Permitir modificar:

- Título.
- Descripción.
- Categoría.
- Rutina.
- Día.
- Horario válido.
- Foto.
- Notas.

### Eliminar tarea

Debe pedir confirmación antes de eliminar. No hay lógica adicional por estado.

---

## 13. CRUD secundario: Rutinas

### Crear rutina

Campos, todos obligatorios salvo donde se indica:

- Nombre/título (obligatorio).
- Descripción (obligatorio).
- Ícono (obligatorio — define la identidad visual de la rutina).
- Place/lugar (obligatorio — se selecciona o crea desde la entidad Place).
- Días (obligatorio — al menos uno).
- Hora de inicio (obligatorio).
- Hora de fin (obligatorio).

Validaciones:

- Nombre no vacío.
- Descripción no vacía.
- Ícono seleccionado.
- Place seleccionado.
- Al menos un día seleccionado.
- Hora inicio menor que hora fin.

### Ver rutinas

Se muestran como cards con:

- Nombre e ícono.
- Días.
- Horario.
- Lugar.
- Cantidad de tareas asociadas.

El listado incluye un **filtro por día de la semana** (Todas / Lun / Mar / Mié / Jue / Vie / Sáb / Dom) que muestra solo las rutinas activas ese día. Este filtro está implementado en el Figma.

### Editar rutina

Permitir modificar:

- Nombre.
- Descripción.
- Ícono.
- Lugar/Place.
- Días.
- Horario.

No se muestra advertencia al editar aunque haya tareas asociadas. La edición es directa.

### Eliminar rutina

Controlar si tiene tareas asociadas.

Opción acordada: confirmar eliminación de rutina junto con sus tareas asociadas.

Mensaje:

> Esta rutina tiene tareas asociadas. Si la eliminás, también se eliminarán sus tareas. ¿Querés continuar?

---

## 14. Geocoding sandbox y entidad Place

La app no usa GPS ni geofencing. Para transformar una dirección en coordenadas se usa un geocoder sandbox local.

### Flujo acordado

1. El usuario ingresa a Crear rutina y necesita asociar un lugar.
2. Escribe una dirección.
3. La app muestra coincidencias desde `demo_places.json` (lista precargada).
4. El usuario selecciona la dirección que corresponde.
5. La app guarda el Place con nombre, dirección, latitud y longitud.
6. Las coordenadas se reutilizan para calcular distancias contra comercios sandbox.

### Defensa de la decisión

> "La app no utiliza GPS ni mapas. Solo usa geocoding al crear un Lugar para transformar una dirección escrita por el usuario en coordenadas. Con esas coordenadas calcula localmente la distancia contra comercios sandbox precargados."

### Alternativa (opcional)

Si el equipo decide agregar geocoding real, Nominatim de OpenStreetMap es la opción más simple y gratuita para una demo. Solo se llama al crear el Place, no en cada apertura de tarea.

---

## 15. Flujos principales

TaskPoint debe tener al menos tres flujos de pantallas.

### Flujo 1 — Crear rutina

Pantallas posibles:

```text
MisRutinasScreen
        ↓
CrearRutinaScreen (incluye selección/creación de Place)
        ↓
MisRutinasScreen actualizado
```

### Flujo 2 — Crear tarea

Pantallas posibles:

```text
Home / TareasDelDiaScreen
        ↓
CrearTareaScreen
        ↓
Selector cámara / galería
        ↓
Home / TareasDelDiaScreen actualizado
```

### Flujo 3 — Ver tareas y detalle

Pantallas posibles:

```text
Home / TareasDelDiaScreen
        ↓
DetalleTareaScreen
        ↓
Ofertas sandbox si aplica
```

Otros flujos posibles:

- Onboarding.
- Login.
- Registro.
- Editar tarea.
- Editar rutina.
- Eliminar tarea/rutina.

---

## 16. Diagramas de secuencia acordados

### Diagrama Crear Rutina

Participantes:

```text
Usuario
CrearRutinaScreen
RutinaViewModel
CrearRutinaUseCase
RutinaRepository
PlaceRepository
Room / DataSource Local
Backend API
```

Flujo:

1. Usuario ingresa a Crear rutina.
2. Screen muestra formulario.
3. Usuario completa nombre, descripción, ícono, lugar (Place), días y horario.
4. Usuario presiona Guardar rutina.
5. Screen envía evento al ViewModel.
6. ViewModel llama a `CrearRutinaUseCase`.
7. UseCase valida campos obligatorios y reglas de creación.
8. Si los datos son inválidos:
   - Devuelve error de validación.
   - ViewModel expone estado error.
   - Screen muestra mensaje.
9. Si los datos son válidos:
   - UseCase llama a Repository.
   - Repository guarda localmente en Room.
   - Repository intenta sincronizar con backend.
   - Si sincroniza bien: `syncStatus = SYNCED`.
   - Si falla la conexión: `syncStatus = PENDING_CREATE`.
10. ViewModel expone estado success.
11. Screen vuelve a Mis rutinas y muestra la rutina creada.

### Diagrama Crear Tarea

Participantes:

```text
Usuario
CrearTareaScreen
TareaViewModel
CrearTareaUseCase
TareaRepository
RutinaRepository
Cámara
Selector imagen / Galería
Room / DataSource Local
Backend API
```

Flujo:

1. Usuario ingresa a Crear tarea.
2. Screen muestra formulario.
3. ViewModel solicita rutinas disponibles.
4. TareaViewModel llama a RutinaRepository.
5. RutinaRepository consulta Room.
6. Se devuelve lista de rutinas.
7. Screen muestra rutinas disponibles.
8. Usuario completa título, descripción y categoría.
9. Usuario selecciona rutina.
10. ViewModel obtiene detalle de la rutina.
11. Screen muestra días y horarios permitidos.
12. Usuario selecciona día y horario.
13. Foto opcional:
    - Si elige cámara: abre cámara y devuelve `fotoUri`.
    - Si elige galería: abre selector y devuelve `fotoUri`.
14. Usuario presiona Guardar tarea.
15. ViewModel llama a `CrearTareaUseCase`.
16. UseCase valida título, categoría, rutina, día y horario dentro del rango.
17. Si los datos son inválidos:
    - Devuelve error.
    - ViewModel expone estado error.
    - Screen muestra mensaje.
18. Si los datos son válidos:
    - Repository guarda tarea localmente en Room.
    - Repository intenta sincronizar con backend.
    - Si sincroniza bien: `syncStatus = SYNCED`.
    - Si falla conexión: `syncStatus = PENDING_CREATE`.
19. ViewModel expone success.
20. Screen vuelve a Mis tareas y muestra la tarea creada.

### Regla importante de diagramas

La sincronización debe ser breve y clara para que el diagrama no se rompa visualmente. No expandir demasiado el bloque `alt` de sincronización.

---

## 17. Card Views

La consigna pide un listado compuesto por Card Views.

En TaskPoint, los listados deben usar Material 3:

- `Card`
- `ElevatedCard`
- `OutlinedCard`

No significa inventar cards desde cero. Significa usar cards de Material 3 para agrupar información.

### Card de tarea

Debe poder mostrar:

- Título.
- Categoría.
- Rutina asociada.
- Día y hora.
- Foto miniatura opcional.
- Acción de ver detalle.

### Card de rutina

Debe poder mostrar:

- Nombre.
- Días.
- Horario.
- Lugar.
- Ícono obligatorio.
- Cantidad de tareas asociadas si aplica.

### Card de oferta sandbox

Debe poder mostrar:

- Comercio.
- Distancia simulada o calculada.
- Oferta.
- Vigencia si aplica.

### Buenas prácticas

- Usar `modifier: Modifier = Modifier` como parámetro.
- Usar `MaterialTheme.colorScheme`.
- Usar `MaterialTheme.typography`.
- Evitar colores hardcodeados.
- Agregar `contentDescription` en imágenes e íconos informativos.
- Usar `LazyColumn` con keys estables.

---

## 18. Material 3, Dark Mode y Dynamic Color

TaskPoint debe usar Material 3.

### MaterialTheme

La app debe tener un `Theme.kt` con:

- `lightColorScheme`.
- `darkColorScheme`.
- Tipografía.
- Shapes si aplica.
- Dynamic color si se decide usar.
- Scaffold.

### Regla

No duplicar pantallas para modo oscuro.

La pantalla debe usar:

```kotlin
MaterialTheme.colorScheme.background
MaterialTheme.colorScheme.surface
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onSurface
MaterialTheme.typography.bodyLarge
```

En vez de colores fijos.

### Dark mode

MaterialTheme ayuda, pero no hace magia si se hardcodean colores. Se debe probar visualmente contraste y legibilidad.

---

## 19. Accesibilidad

TaskPoint debe aplicar buenas prácticas de accesibilidad.

### Reglas mínimas

- Usar textos en `sp`.
- Usar `MaterialTheme.typography`.
- No usar `px` para textos.
- Agregar `contentDescription` en imágenes e íconos que comunican información o tienen acción.
- Si una imagen es decorativa, usar `contentDescription = null`.
- Usar labels claros en `TextField`.
- Los botones deben tener texto claro.
- Los elementos táctiles deben tener tamaño suficiente.
- Mostrar errores comprensibles.
- Usar contraste adecuado.
- Respetar jerarquía visual.
- Mantener la app usable con una mano.
- Considerar contexto móvil: distracciones, movilidad, pantalla limitada y conectividad variable.

---

## 20. Internacionalización

La internacionalización es opcional según la consigna.

Pero si se implementa, debe hacerse bien:

- No hardcodear textos en composables.
- Usar `stringResource(R.string.xxx)`.
- Crear `strings.xml`.
- Preparar español como idioma base.
- Opcionalmente agregar inglés.
- Incluir textos de:
  - Botones.
  - Labels.
  - Errores.
  - Títulos.
  - Content descriptions.
  - Mensajes de snackbar.
  - Estados vacíos.

No mencionar internacionalización en documentación si el código no la implementa coherentemente.

---

## 21. UI/UX y heurísticas de Nielsen

TaskPoint debe evidenciar:

- Mapa de navegación.
- Design system básico.
- Prototipo navegable en Figma.
- Checklist de heurísticas de Nielsen.
- Buenas prácticas de accesibilidad.

### Principios UI/UX a respetar

- Usabilidad.
- Consistencia.
- Simplicidad.
- Feedback.
- Jerarquía visual.
- Accesibilidad.
- Testeo y prototipado.
- Flexibilidad y adaptabilidad.
- Diseño centrado en el usuario.

### Heurísticas de Nielsen

Debe existir checklist que evidencie cómo la app cumple, por ejemplo:

1. Visibilidad del estado del sistema.
2. Relación entre sistema y mundo real.
3. Control y libertad del usuario.
4. Consistencia y estándares.
5. Prevención de errores.
6. Reconocimiento antes que recuerdo.
7. Flexibilidad y eficiencia de uso.
8. Diseño estético y minimalista.
9. Ayuda para reconocer, diagnosticar y recuperarse de errores.
10. Ayuda y documentación.

---

## 22. Sandbox comercial / monetización

TaskPoint puede mostrar sugerencias comerciales contextuales según:

```text
Categoría de tarea + coordenadas del Place asociado a la rutina
```

Ejemplo:

- Tarea: Comprar comida.
- Categoría: Supermercado.
- Rutina: Trabajo presencial.
- Place: Av. Santa Fe 3000 (lat: -34.5889 / lon: -58.4101).

La app muestra:

- DIA - Av. Santa Fe 3050 - 120 m.
- Carrefour Express - Av. Santa Fe 2920 - 180 m.
- Market local - 190 m.

Con ofertas simuladas:

- 20% en productos seleccionados.
- 2x1 en bebidas.
- 15% con billetera virtual.

### Reglas

- No usar Google Maps.
- Usar datos sandbox precargados en JSON → Room.
- No leer JSON directamente desde la UI.
- Precargar JSON en Room o consultarlo mediante Repository.
- Calcular distancia usando coordenadas del Place y coordenadas del Store.
- Justificarlo como dataset sandbox para demo y modelo de negocio.

---

## 23. Estructura recomendada del proyecto

Una estructura defendible:

```text
app/src/main/java/com/taskpoint/

ui/
  screens/
    onboarding/
      OnboardingScreen.kt
      OnboardingContent.kt
    auth/
      LoginScreen.kt
      LoginContent.kt
    home/
      HomeScreen.kt
      HomeContent.kt
    task/
      CrearTareaScreen.kt
      CrearTareaContent.kt
      DetalleTareaScreen.kt
      DetalleTareaContent.kt
    routine/
      CrearRutinaScreen.kt
      CrearRutinaContent.kt
      MisRutinasScreen.kt
      MisRutinasContent.kt
    place/
      CrearPlaceScreen.kt
      CrearPlaceContent.kt
  components/
    TaskCard.kt
    RoutineCard.kt
    OfferCard.kt
    EmptyState.kt
    ErrorMessage.kt
  navigation/
    AppNavigation.kt
    Routes.kt
  theme/
    Color.kt
    Theme.kt
    Type.kt
  state/
    UiText.kt

domain/
  model/
    Task.kt
    Routine.kt
    Place.kt
    Category.kt
    Store.kt
    Offer.kt
    SyncStatus.kt
  usecase/
    CrearTareaUseCase.kt
    CrearRutinaUseCase.kt
    CrearPlaceUseCase.kt
    ObtenerRutinasUseCase.kt
    ObtenerTareasDelDiaUseCase.kt
    ObtenerOfertasParaTareaUseCase.kt

data/
  repository/
    TaskRepositoryImpl.kt
    RoutineRepositoryImpl.kt
    PlaceRepositoryImpl.kt
    OfferRepositoryImpl.kt
  local/
    room/
      dao/
        TaskDao.kt
        RoutineDao.kt
        PlaceDao.kt
        CategoryDao.kt
        StoreDao.kt
        OfferDao.kt
      entity/
        TaskEntity.kt
        RoutineEntity.kt
        PlaceEntity.kt
        CategoryEntity.kt
        StoreEntity.kt
        OfferEntity.kt
      database/
        TaskPointDatabase.kt
  remote/
    api/
      TaskApi.kt
      RoutineApi.kt
    dto/
      TaskDto.kt
      RoutineDto.kt
  mapper/
    TaskMapper.kt
    RoutineMapper.kt
    PlaceMapper.kt
    OfferMapper.kt
  sandbox/
    SandboxSeeder.kt
    SandboxAssetDataSource.kt
    GeocoderSandbox.kt

di/
  AppContainer.kt
```

Si el proyecto es inicial o mockeado, se puede simplificar, pero sin mezclar responsabilidades en `MainActivity`.

---

## 24. Buenas prácticas Android / Compose

Se debe respetar:

- Composables rápidos y predecibles.
- No hacer trabajo pesado en composables.
- No acceder a datos desde composables.
- No poner validaciones extensas dentro de `onClick`.
- `onClick` solo dispara eventos.
- Usar ViewModel para estado.
- Usar UseCase para validaciones.
- Usar Repository para datos.
- Usar Room como fuente local.
- Usar Retrofit/Gson o similar para backend.
- Usar `LazyColumn` con keys estables.
- Usar `remember` solo cuando corresponde.
- Usar `derivedStateOf` solo cuando hay estado derivado real.
- Usar callbacks específicos para navegación.
- Evitar pasar `NavController` a todos los composables.
- Usar `Scaffold` en pantallas principales.
- Usar `SnackbarHost` para mensajes temporales.
- Manejar loading, success y error.
- Mostrar errores de conectividad de forma clara.
- No hardcodear colores ni textos si se implementa i18n.

---

## 25. Errores que se deben evitar

Evitar:

- God Composable.
- MainActivity con toda la app.
- ViewModel obeso.
- Repository anémico sin criterio.
- UI accediendo a Room.
- UI accediendo a Retrofit.
- Validaciones de negocio en la UI.
- Estado duplicado.
- Side effects sin control.
- Navegación mezclada en componentes reutilizables.
- Hardcodear colores fuera del tema.
- Hardcodear textos si se documenta internacionalización.
- Mezclar entidades Room directamente en UI.
- Leer JSON sandbox directamente desde composables.
- Prometer GPS/geofencing real.
- Prometer sincronización compleja no implementada.
- Mostrar "estado de tarea" en UI — las tareas no tienen estado visible.
- Agregar advertencias al editar rutina — no está diseñado en Figma.

---

## 26. Coherencia con Figma, manual y documentación

Hay que mantener coherencia estricta entre:

- Figma.
- Manual de usuario.
- Documentación técnica.
- Diagramas de secuencia.
- Código.

Reglas acordadas:

- El Home muestra tareas del día agrupadas bajo su rutina con header de grupo.
- No agregar "Rutinas próximas" si no está en Figma.
- No agregar "Estado de sincronización" visible en Home si no está en Figma.
- En Crear Rutina, el ícono y la descripción son obligatorios.
- No escribir "ícono si la app lo permite".
- Las tareas no tienen estado visible en UI (sin pendiente/completado).
- No agregar advertencia al editar rutina con tareas — no está en Figma.
- El filtro por día en Mis Rutinas está en Figma y debe estar documentado.
- Place es una entidad propia. No es un campo directo de Routine.
- Si algo no está en Figma, o se elimina del manual o se agrega conscientemente al prototipo/documentación.

---

## 27. Reglas para recibir ayuda con código

Cuando se trabaje con código de TaskPoint:

1. Indicar si hay que crear carpeta.
2. Indicar si hay que crear archivo nuevo.
3. Indicar si hay que reemplazar un archivo existente.
4. Pedir el archivo actual cuando sea necesario para no romper el proyecto.
5. Mantener Kotlin + Jetpack Compose + Material 3.
6. Mantener MVVM + Repository + UseCase.
7. No resolver todo dentro de `MainActivity`.
8. No mezclar UI, datos y lógica de negocio.
9. Dar código listo para copiar y pegar cuando sea posible.
10. Ser crítico si algo contradice la consigna, los apuntes o el Figma.

---

## 28. Resumen ejecutivo para defensa

TaskPoint es una app Android desarrollada en Kotlin y Jetpack Compose que permite organizar tareas según la rutina semanal declarada por el usuario. A diferencia de soluciones basadas en geolocalización constante, TaskPoint trabaja sobre rutinas, lugares y horarios cargados por el usuario, evitando depender de GPS o geofencing.

La arquitectura propuesta sigue MVVM, Repository, UseCases y persistencia local con Room, combinada con sincronización básica hacia backend. El enfoque es offline first: la app funciona con datos locales y marca cambios pendientes mediante `syncStatus` cuando no hay conectividad.

La entidad Place permite asociar coordenadas a cada rutina sin GPS. Un geocoder sandbox local transforma las direcciones ingresadas en coordenadas, que luego se usan para calcular distancias contra comercios sandbox precargados.

La UI se construye con Material 3, Jetpack Compose, Screen/Content, StateFlow, UDF y componentes reutilizables. Se contemplan accesibilidad, dark mode, Card Views, cámara/galería para adjuntar fotos, y un módulo sandbox de sugerencias comerciales basado en categorías y coordenadas del Place asociado a cada rutina.

La solución busca ser técnicamente defendible, mantenible, coherente con los patrones vistos en clase y alineada con los requisitos del Trabajo Práctico Integrador.
