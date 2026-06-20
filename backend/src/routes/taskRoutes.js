import { Router } from "express";

export const taskRouter = Router();

taskRouter.get("/", (_req, res) => {
  res.status(501).json({ message: "List tasks endpoint pending implementation." });
});

taskRouter.post("/", (_req, res) => {
  res.status(501).json({ message: "Create task endpoint pending implementation." });
});

taskRouter.put("/:id", (_req, res) => {
  res.status(501).json({ message: "Update task endpoint pending implementation." });
});

taskRouter.delete("/:id", (_req, res) => {
  res.status(501).json({ message: "Delete task endpoint pending implementation." });
});
