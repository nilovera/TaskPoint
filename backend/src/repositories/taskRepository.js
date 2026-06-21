import { getFirestore } from "../config/firebase.js";

const COLLECTION = "tasks";

export async function listTasks(userId) {
  return (await listTaskSyncRecords(userId)).filter(task => !task.deleted);
}

export async function listTaskSyncRecords(userId) {
  const snapshot = await collectionFor(userId).orderBy("updatedAt", "desc").get();
  return snapshot.docs.map(fromDocument);
}

export async function saveTaskLastWriteWins(userId, task) {
  const firestore = getFirestore();
  const document = collectionFor(userId).doc(task.id);

  return firestore.runTransaction(async transaction => {
    const snapshot = await transaction.get(document);
    const existing = snapshot.exists ? fromDocument(snapshot) : null;

    if (existing && existing.updatedAt > task.updatedAt) {
      return {
        task: existing.deleted ? null : existing,
        deleted: Boolean(existing.deleted),
        created: false
      };
    }

    const savedTask = {
      ...task,
      deleted: false,
      createdAt: existing?.createdAt ?? task.createdAt
    };
    transaction.set(document, savedTask);
    return { task: savedTask, deleted: false, created: !existing };
  });
}

export async function deleteTaskLastWriteWins(userId, taskId, updatedAt) {
  const firestore = getFirestore();
  const document = collectionFor(userId).doc(taskId);
  return firestore.runTransaction(async transaction => {
    const snapshot = await transaction.get(document);
    const existing = snapshot.exists ? fromDocument(snapshot) : null;

    if (existing && existing.updatedAt > updatedAt) {
      return {
        deleted: Boolean(existing.deleted),
        applied: false,
        task: existing.deleted ? null : existing
      };
    }

    transaction.set(document, {
      id: taskId,
      deleted: true,
      updatedAt,
      createdAt: existing?.createdAt ?? Date.now()
    });
    return { deleted: true, applied: true, task: null };
  });
}

function collectionFor(userId) {
  return getFirestore().collection("users").doc(userId).collection(COLLECTION);
}

function fromDocument(document) {
  return { id: document.id, ...document.data() };
}
