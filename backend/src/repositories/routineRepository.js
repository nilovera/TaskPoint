import { getFirestore } from "../config/firebase.js";

const COLLECTION = "routines";

export async function listRoutines(userId) {
  const snapshot = await collectionFor(userId).orderBy("updatedAt", "desc").get();
  return snapshot.docs.map(fromDocument);
}

export async function saveRoutineLastWriteWins(userId, routine) {
  const firestore = getFirestore();
  const document = collectionFor(userId).doc(routine.id);

  return firestore.runTransaction(async transaction => {
    const snapshot = await transaction.get(document);
    const existing = snapshot.exists ? fromDocument(snapshot) : null;

    if (existing && existing.updatedAt > routine.updatedAt) {
      return { routine: existing, created: false };
    }

    const savedRoutine = {
      ...routine,
      createdAt: existing?.createdAt ?? routine.createdAt
    };
    transaction.set(document, savedRoutine);
    return { routine: savedRoutine, created: !existing };
  });
}

export async function deleteRoutine(userId, routineId) {
  const document = collectionFor(userId).doc(routineId);
  const snapshot = await document.get();
  if (!snapshot.exists) return false;

  await document.delete();
  return true;
}

function collectionFor(userId) {
  return getFirestore().collection("users").doc(userId).collection(COLLECTION);
}

function fromDocument(document) {
  return { id: document.id, ...document.data() };
}
