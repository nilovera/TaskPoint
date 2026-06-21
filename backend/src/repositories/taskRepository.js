import { getFirestore } from "../config/firebase.js";

const COLLECTION = "tasks";

export async function listTasks(userId) {
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
      return { task: existing, created: false };
    }

    const savedTask = {
      ...task,
      createdAt: existing?.createdAt ?? task.createdAt
    };
    transaction.set(document, savedTask);
    return { task: savedTask, created: !existing };
  });
}

export async function deleteTask(userId, taskId) {
  const document = collectionFor(userId).doc(taskId);
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
