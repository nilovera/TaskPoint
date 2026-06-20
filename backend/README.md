# TaskPoint Backend

Backend REST para TaskPoint.

## Requisitos

- Node.js 20 o superior.
- npm.

## Configuracion

1. Copiar `.env.example` a `.env`.
2. Ajustar variables si hace falta.

```bash
cp .env .env
```

En Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Variables recomendadas en `.env`:

```env
PORT=8080
NODE_ENV=development
APP_NAME=TaskPoint API
JWT_SECRET=usar-un-secreto-largo-local
JWT_EXPIRES_IN=7d
```

## Firebase

El backend se conecta a Firestore usando Firebase Admin SDK.

No subas credenciales al repositorio. Usar una de estas opciones en `.env`:

### Opcion A: archivo service account local

```env
FIREBASE_SERVICE_ACCOUNT_PATH=C:\ruta\privada\taskpoint-service-account.json
```

### Opcion B: variables sueltas

```env
FIREBASE_PROJECT_ID=tu-project-id
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@tu-project-id.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
```

Para generar el service account:

1. Entrar a Firebase Console.
2. Ir a Project settings.
3. Service accounts.
4. Generate new private key.
5. Guardar el JSON fuera del repositorio.

## Instalacion

```bash
npm install
```

## Ejecutar en desarrollo

```bash
npm run dev
```

La API queda disponible en:

```text
http://localhost:8080
```

Desde el emulador Android, la app accede a la PC con:

```text
http://10.0.2.2:8080
```

## Health check

```text
GET /health
```

Respuesta esperada:

```json
{
  "status": "ok",
  "service": "taskpoint-api"
}
```

## Firebase health check

Requiere credenciales Firebase configuradas.

```text
GET /health/firebase
```

Respuesta esperada:

```json
{
  "status": "ok",
  "service": "firebase",
  "projectId": "tu-project-id",
  "checkedAt": "2026-06-20T00:00:00.000Z"
}
```

## Proximas etapas

1. Implementar endpoints de rutinas y tareas.
2. Conectar sincronizacion local-first desde Android.

## Auth

### Register

```text
POST /auth/register
Content-Type: application/json
```

```json
{
  "name": "Tomas",
  "email": "tomas@mail.com",
  "password": "123456"
}
```

Respuesta:

```json
{
  "token": "jwt",
  "user": {
    "id": "uuid",
    "name": "Tomas",
    "email": "tomas@mail.com"
  }
}
```

### Login

```text
POST /auth/login
Content-Type: application/json
```

```json
{
  "email": "tomas@mail.com",
  "password": "123456"
}
```

### Current user

```text
GET /auth/me
Authorization: Bearer <token>
```

Las contrasenas se guardan en Firestore como hash bcrypt, nunca como texto plano.

### Recuperacion de contrasena en desarrollo

Mientras no exista envio de email, el codigo de recuperacion de desarrollo es:

```text
123456
```
