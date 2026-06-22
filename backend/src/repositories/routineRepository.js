import { getFirestore } from "../config/firebase.js";

const COLLECTION = "routines";

export async function listRoutines(userId) {
  return (await listRoutineSyncRecords(userId)).filter(routine => !routine.deleted);
}

export async function listRoutineSyncRecords(userId) {
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
      return {
        routine: existing.deleted ? null : existing,
        deleted: Boolean(existing.deleted),
        created: false
      };
    }

    const savedRoutine = {
      ...routine,
      deleted: false,
      createdAt: existing?.createdAt ?? routine.createdAt
    };
    transaction.set(document, savedRoutine);
    return { routine: savedRoutine, deleted: false, created: !existing };
  });
}

export async function deleteRoutineLastWriteWins(userId, routineId, updatedAt) {
  const firestore = getFirestore();
  const document = collectionFor(userId).doc(routineId);
  return firestore.runTransaction(async transaction => {
    const snapshot = await transaction.get(document);
    const existing = snapshot.exists ? fromDocument(snapshot) : null;

    if (existing && existing.updatedAt > updatedAt) {
      return {
        deleted: Boolean(existing.deleted),
        applied: false,
        routine: existing.deleted ? null : existing
      };
    }

    transaction.set(document, {
      id: routineId,
      deleted: true,
      updatedAt,
      createdAt: existing?.createdAt ?? Date.now()
    });
    return { deleted: true, applied: true, routine: null };
  });
}

function collectionFor(userId) {
  return getFirestore().collection("users").doc(userId).collection(COLLECTION);
}

function fromDocument(document) {
  return { id: document.id, ...document.data() };
}
