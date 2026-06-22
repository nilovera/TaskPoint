import { Router } from "express";
import { requireAuth } from "../middleware/authMiddleware.js";
import { createRoutine, getRoutineSyncRecords, getRoutines, removeRoutine, updateRoutine } from "../services/routineService.js";
import { asyncHandler } from "../utils/asyncHandler.js";
import { logSyncEvent } from "../utils/syncLogger.js";

export const routineRouter = Router();

routineRouter.use(requireAuth);

routineRouter.get(
  "/",
  asyncHandler(async (req, res) => {
    res.json(await getRoutines(req.user.id));
  })
);

routineRouter.get(
  "/sync",
  asyncHandler(async (req, res) => {
    const records = await getRoutineSyncRecords(req.user.id);
    logSyncEvent("pull_routines", { user: req.user.id.slice(0, 8), count: records.length });
    res.json(records);
  })
);

routineRouter.post(
  "/",
  asyncHandler(async (req, res) => {
    const { routine, created } = await createRoutine(req.user.id, req.body);
    logSyncEvent("push_routine", { user: req.user.id.slice(0, 8), action: "create", id: routine.id.slice(0, 8) });
    res.status(created ? 201 : 200).json(routine);
  })
);

routineRouter.put(
  "/:id",
  asyncHandler(async (req, res) => {
    const { routine } = await updateRoutine(req.user.id, req.params.id, req.body);
    logSyncEvent("push_routine", { user: req.user.id.slice(0, 8), action: "update", id: routine.id.slice(0, 8) });
    res.json(routine);
  })
);

routineRouter.delete(
  "/:id",
  asyncHandler(async (req, res) => {
    await removeRoutine(req.user.id, req.params.id, req.body);
    logSyncEvent("push_routine", { user: req.user.id.slice(0, 8), action: "delete", id: req.params.id.slice(0, 8) });
    res.status(204).send();
  })
);
