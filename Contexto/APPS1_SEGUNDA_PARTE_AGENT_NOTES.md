# Apps1 Segunda Parte - Notas de implementacion para TaskPoint

Fuente leida: `C:\Proyectos\TaskPoint\Contexto\Apps1 Segunda parte.docx`

Estas notas condensan el material de la materia para orientar futuras implementaciones de TaskPoint.

## Reglas base

- La UI en Compose debe renderizar estado, capturar acciones del usuario y emitir callbacks/eventos. No debe tener logica de negocio, consultas SQL, llamadas HTTP, persistencia directa ni procesos pesados.
- Separar `Screen` y `Content` cuando una pantalla crece: `Screen` conecta con ViewModel, observa `StateFlow`, maneja effects/navegacion; `Content` recibe estado y callbacks y se mantiene stateless cuando sea posible.
- El ViewModel coordina la pantalla: mantiene `UiState`, procesa eventos, valida lo necesario para la vista, lanza corrutinas con `viewModelScope` e invoca repositorios/use cases. No debe conocer detalles de Room, Retrofit o archivos.
- El Repository oculta el origen de datos y entrega una API limpia al ViewModel. Puede combinar Room, red, cache o archivos, pero esos detalles no deben filtrarse a la UI.
- Usar modelos separados cuando corresponda: DTO para red, Entity para Room y Domain Model para la logica de la app. Usar mappers entre capas.
- SharedPreferences queda reservado para configuraciones simples o flags pequenos. No guardar objetos serializados ni colecciones pesadas alli.
- Para datos estructurados y persistentes, priorizar Room: `Entity`, `DAO`, `Database` singleton, operaciones `suspend` y consultas con `Flow`.
- Room no debe operar en el Main Thread. Las escrituras como `@Insert`, `@Update` y `@Delete` deben ir en funciones `suspend`; las lecturas observables suelen devolver `Flow<List<Entity>>` directamente para que Room emita cambios.
- El flujo observable recomendado es `Room Flow -> Repository -> ViewModel StateFlow -> Compose`.
- Retrofit debe vivir detras de una interfaz API y un DataSource/Repository. La UI nunca llama Retrofit directo.
- Las respuestas de red necesitan manejo explicito de loading, success y error. Revisar DTOs si falla el parseo de JSON, especialmente tipos y nulabilidad.
- Si se usan interceptors de OkHttp, usarlos para logs, headers, tokens o configuracion comun. No hardcodear secretos.
- Para camara, priorizar CameraX. Vincular los use cases al lifecycle con `bindToLifecycle` y liberar recursos correctamente.
- Para sensores, usar componentes lifecycle-aware. Suscribirse y desuscribirse respetando `onResume/onPause`, `onStart/onStop` o el owner correspondiente.
- Todo recurso abierto en un callback de ciclo de vida debe cerrarse en su contraparte.
- No asumir que la Activity vive para siempre. El estado importante debe estar en ViewModel, Room o una capa persistente; no en atributos efimeros de Activity.
- Evitar operaciones pesadas en `onPause`; debe ser rapido para no trabar transiciones.
- APK sirve para demo o entrega academica. Para publicacion formal en Google Play, el formato esperado es AAB.

## Aplicacion concreta a TaskPoint

- TaskPoint debe evolucionar desde datos demo/mock hacia una arquitectura MVVM con Repository y persistencia local real.
- Room debe ser la prioridad para rutinas, tareas, categorias, lugares, usuarios locales/sesion y metadatos de fotos.
- Si hay backend o API externa, Retrofit debe entrar por `data/remote` y no desde composables ni ViewModels.
- El modo offline deberia apoyarse en Room como fuente local principal. La UI observa Room mediante Repository y StateFlow.
- La validacion de negocio importante pertenece fuera de la UI: por ejemplo, que una tarea respete dia y horario de la rutina asociada.
- Los use cases son utiles cuando encapsulan reglas reales de TaskPoint. No conviene crear use cases vacios que solo deleguen sin agregar valor.
- La estructura sugerida por el material separa carpetas como `data/dto`, `data/network`, `data/local`, `data/repository`, `domain/model` y `presentation` o `ui`.
- Las pantallas grandes deben dividirse en componentes chicos y stateless para evitar God Composables.
- La captura de foto de tarea debe implementarse con CameraX o un flujo de captura lifecycle-aware, nunca como manejo manual pesado dentro de la UI.
- Los errores de persistencia, red, permisos y camara deben reflejarse en `UiState` o one-shot effects, no como crashes silenciosos.
- Para nuevas features, el flujo preferido es:

```text
Composable / Screen
        -> ViewModel
        -> UseCase si hay regla de negocio clara
        -> Repository
        -> DataSource local/remoto
        -> Room / Retrofit / archivo
```

## Prioridad para futuras tareas

1. Mantener Compose liviano y declarativo.
2. Migrar datos mock o JSON a repositorios con Room cuando la feature necesite persistencia real.
3. Exponer estados de pantalla con `UiState` y `StateFlow`.
4. Encapsular reglas del dominio TaskPoint fuera de las pantallas.
5. Manejar ciclo de vida en camara, sensores y corrutinas.
6. Diseñar pensando en offline first y recuperacion ante cierre/recreacion de Activity.
