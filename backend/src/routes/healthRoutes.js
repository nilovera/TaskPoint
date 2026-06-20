import { Router } from "express";
import { getFirestore } from "../config/firebase.js";

export const healthRouter = Router();

healthRouter.get("/", (_req, res) => {
  res.json({
    status: "ok",
    service: "taskpoint-api"
  });
});

healthRouter.get("/firebase", async (_req, res, next) => {
  try {
    const firestore = getFirestore();
    const checkedAt = new Date().toISOString();
    const ref = firestore.collection("_health").doc("backend");

    await ref.set({ checkedAt }, { merge: true });
    const snapshot = await ref.get();

    res.json({
      status: "ok",
      service: "firebase",
      projectId: snapshot.ref.firestore.projectId,
      checkedAt: snapshot.data()?.checkedAt || checkedAt
    });
  } catch (error) {
    next(error);
  }
});
