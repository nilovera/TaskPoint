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

## Configuración de IP

Por default, la app Android debe usar la IP especial del emulador:

```text
http://10.0.2.2:8080
```

Esa direccion permite que el emulador acceda al backend que esta corriendo en la misma PC. Aunque el backend se levante como `localhost`, dentro del emulador `localhost` apunta al propio emulador, no a la computadora.

Si se quiere probar la app desde un celular fisico, se debe reemplazar esa URL por la IPv4 de la PC donde esta corriendo el backend, por ejemplo:

```text
http://192.168.x.x:8080
```

En ese caso, la PC y el celular tienen que estar conectados a la misma red WiFi, y el firewall de la PC debe permitir conexiones entrantes al puerto `8080`.

### ¿Cómo saber la IP de la PC?

En Windows, abrir una terminal y ejecutar:

```powershell
ipconfig
```

Buscar la red que se esta usando, por ejemplo WiFi, y copiar el valor de `Dirección IPv4`. Esa direccion es la que se debe usar en la app cuando se prueba desde un celular fisico.

Esperar el mensaje `Firebase is ready.` antes de abrir la app. Si Firebase no puede conectarse, el backend no inicia para evitar que el primer login falle por una conexion en frio.

Para comprobarlo:

```text
http://localhost:8080/health
http://localhost:8080/health/firebase
```
