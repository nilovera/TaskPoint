# TaskPoint Backend

## Requisitos

- Node.js 20 o superior.
- npm.
- Credenciales locales de Firebase Admin.

## Configuracion

Dentro de `backend/`, crear un archivo local llamado `.env` con los valores necesarios:

```env
PORT=8080
NODE_ENV=development
APP_NAME=TaskPoint API
JWT_SECRET=un-secreto-largo-y-privado
JWT_EXPIRES_IN=7d

FIREBASE_SERVICE_ACCOUNT_PATH=C:\ruta\privada\taskpoint-service-account.json

SMTP_HOST=smtp.gmail.com
SMTP_PORT=465
SMTP_SECURE=true
SMTP_USER=tu-correo@gmail.com
SMTP_PASSWORD=tu-contrasena-de-aplicacion
SMTP_FROM=TaskPoint <tu-correo@gmail.com>
```

No subir `.env` ni el archivo de credenciales de Firebase a Git.

## Levantar el backend

```powershell
cd backend
npm install
npm run dev
```

El backend queda disponible en:

```text
http://localhost:8080
```

Esperar el mensaje `Firebase is ready.` antes de abrir la app. Si Firebase no puede conectarse, el backend no inicia para evitar que el primer login falle por una conexion en frio.

Para comprobarlo:

```text
http://localhost:8080/health
http://localhost:8080/health/firebase
```
