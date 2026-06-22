import { createApp } from "./app.js";
import { env } from "./config/env.js";
import { getFirestore } from "./config/firebase.js";

const app = createApp();

async function startServer() {
  try {
    // Fuerza la autenticacion inicial con Firestore antes de aceptar logins.
    // Asi el primer request de la app no absorbe el arranque en frio de Firebase.
    await getFirestore().collection("_health").doc("backend").get();
    console.log("Firebase is ready.");

    app.listen(env.port, () => {
      console.log(`${env.appName} listening on http://localhost:${env.port}`);
    });
  } catch (error) {
    console.error("Firebase startup check failed. Server was not started.", error);
    process.exitCode = 1;
  }
}

startServer();
