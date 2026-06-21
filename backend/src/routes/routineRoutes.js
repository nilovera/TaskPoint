import { Router } from "express";
import { requireAuth } from "../middleware/authMiddleware.js";
import { createRoutine, getRoutines, removeRoutine, updateRoutine } from "../services/routineService.js";
import { asyncHandler } from "../utils/asyncHandler.js";

export const routineRouter = Router();

routineRouter.use(requireAuth);

routineRouter.get(
  "/",
  asyncHandler(async (req, res) => {
    res.json(await getRoutines(req.user.id));
  })
);

routineRouter.post(
  "/",
  asyncHandler(async (req, res) => {
    const { routine, created } = await createRoutine(req.user.id, req.body);
    res.status(created ? 201 : 200).json(routine);
  })
);

routineRouter.put(
  "/:id",
  asyncHandler(async (req, res) => {
    res.json(await updateRoutine(req.user.id, req.params.id, req.body));
  })
);

routineRouter.delete(
  "/:id",
  asyncHandler(async (req, res) => {
    await removeRoutine(req.user.id, req.params.id);
    res.status(204).send();
  })
);
