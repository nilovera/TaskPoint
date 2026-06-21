import { randomUUID } from "node:crypto";
import { getFirestore } from "../config/firebase.js";

const COLLECTION = "users";

export async function findUserByEmail(email) {
  const firestore = getFirestore();
  const emailLower = normalizeEmail(email);
  const snapshot = await firestore
    .collection(COLLECTION)
    .where("emailLower", "==", emailLower)
    .limit(1)
    .get();

  if (snapshot.empty) return null;

  const doc = snapshot.docs[0];
  return fromDocument(doc);
}

export async function findUserById(id) {
  const firestore = getFirestore();
  const doc = await firestore.collection(COLLECTION).doc(id).get();
  if (!doc.exists) return null;
  return fromDocument(doc);
}

export async function createUser({ name, email, passwordHash }) {
  const firestore = getFirestore();
  const id = randomUUID();
  const now = Date.now();
  const emailLower = normalizeEmail(email);

  const user = {
    id,
    name,
    email,
    emailLower,
    passwordHash,
    createdAt: now,
    updatedAt: now
  };

  await firestore.collection(COLLECTION).doc(id).set(user);
  return user;
}

export async function updateUserPassword(id, passwordHash) {
  const firestore = getFirestore();
  const updatedAt = Date.now();

  await firestore.collection(COLLECTION).doc(id).update({
    passwordHash,
    updatedAt
  });

  return findUserById(id);
}

export async function deleteUser(id) {
  const firestore = getFirestore();
  const userDocument = firestore.collection(COLLECTION).doc(id);

  await deleteCollection(userDocument.collection("tasks"));
  await deleteCollection(userDocument.collection("routines"));
  await userDocument.delete();
}

async function deleteCollection(collection) {
  while (true) {
    const snapshot = await collection.limit(400).get();
    if (snapshot.empty) return;

    const batch = getFirestore().batch();
    snapshot.docs.forEach(document => batch.delete(document.ref));
    await batch.commit();
  }
}

function normalizeEmail(email) {
  return email.trim().toLowerCase();
}

function fromDocument(doc) {
  return {
    id: doc.id,
    ...doc.data()
  };
}
