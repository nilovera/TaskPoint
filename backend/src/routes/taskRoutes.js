import { Router } from "express";
import { requireAuth } from "../middleware/authMiddleware.js";
import { createTask, getTasks, removeTask, updateTask } from "../services/taskService.js";
import { asyncHandler } from "../utils/asyncHandler.js";

export const taskRouter = Router();

taskRouter.use(requireAuth);

taskRouter.get(
  "/",
  asyncHandler(async (req, res) => {
    res.json(await getTasks(req.user.id));
  })
);

taskRouter.post(
  "/",
  asyncHandler(async (req, res) => {
    const { task, created } = await createTask(req.user.id, req.body);
    res.status(created ? 201 : 200).json(task);
  })
);

taskRouter.put(
  "/:id",
  asyncHandler(async (req, res) => {
    res.json(await updateTask(req.user.id, req.params.id, req.body));
  })
);

taskRouter.delete(
  "/:id",
  asyncHandler(async (req, res) => {
    await removeTask(req.user.id, req.params.id);
    res.status(204).send();
  })
);
