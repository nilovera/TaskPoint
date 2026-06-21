import { Router } from "express";
import { requireAuth } from "../middleware/authMiddleware.js";
import { createTask, getTaskSyncRecords, getTasks, removeTask, updateTask } from "../services/taskService.js";
import { asyncHandler } from "../utils/asyncHandler.js";
import { logSyncEvent } from "../utils/syncLogger.js";

export const taskRouter = Router();

taskRouter.use(requireAuth);

taskRouter.get(
  "/",
  asyncHandler(async (req, res) => {
    res.json(await getTasks(req.user.id));
  })
);

taskRouter.get(
  "/sync",
  asyncHandler(async (req, res) => {
    const records = await getTaskSyncRecords(req.user.id);
    logSyncEvent("pull_tasks", { user: req.user.id.slice(0, 8), count: records.length });
    res.json(records);
  })
);

taskRouter.post(
  "/",
  asyncHandler(async (req, res) => {
    const { task, created } = await createTask(req.user.id, req.body);
    logSyncEvent("push_task", { user: req.user.id.slice(0, 8), action: "create", id: task.id.slice(0, 8) });
    res.status(created ? 201 : 200).json(task);
  })
);

taskRouter.put(
  "/:id",
  asyncHandler(async (req, res) => {
    const { task } = await updateTask(req.user.id, req.params.id, req.body);
    logSyncEvent("push_task", { user: req.user.id.slice(0, 8), action: "update", id: task.id.slice(0, 8) });
    res.json(task);
  })
);

taskRouter.delete(
  "/:id",
  asyncHandler(async (req, res) => {
    await removeTask(req.user.id, req.params.id, req.body);
    logSyncEvent("push_task", { user: req.user.id.slice(0, 8), action: "delete", id: req.params.id.slice(0, 8) });
    res.status(204).send();
  })
);
