import { cert, getApps, initializeApp } from "firebase-admin/app";
import { getFirestore as getAdminFirestore } from "firebase-admin/firestore";
import fs from "node:fs";
import { env } from "./env.js";

let firestoreInstance = null;

export function getFirestore() {
  if (firestoreInstance) return firestoreInstance;

  const credential = createCredential();

  if (getApps().length === 0) {
    initializeApp({ credential });
  }

  firestoreInstance = getAdminFirestore();
  return firestoreInstance;
}

function createCredential() {
  if (env.firebase.serviceAccountPath) {
    const raw = fs.readFileSync(env.firebase.serviceAccountPath, "utf8");
    return cert(JSON.parse(raw));
  }

  if (
    env.firebase.projectId &&
    env.firebase.clientEmail &&
    env.firebase.privateKey
  ) {
    return cert({
      projectId: env.firebase.projectId,
      clientEmail: env.firebase.clientEmail,
      privateKey: env.firebase.privateKey.replace(/\\n/g, "\n")
    });
  }

  throw new Error(
    "Firebase credentials are missing. Configure FIREBASE_SERVICE_ACCOUNT_PATH or FIREBASE_PROJECT_ID/FIREBASE_CLIENT_EMAIL/FIREBASE_PRIVATE_KEY."
  );
}
