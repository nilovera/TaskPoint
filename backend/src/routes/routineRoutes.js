import { Router } from "express";

export const routineRouter = Router();

routineRouter.get("/", (_req, res) => {
  res.status(501).json({ message: "List routines endpoint pending implementation." });
});

routineRouter.post("/", (_req, res) => {
  res.status(501).json({ message: "Create routine endpoint pending implementation." });
});

routineRouter.put("/:id", (_req, res) => {
  res.status(501).json({ message: "Update routine endpoint pending implementation." });
});

routineRouter.delete("/:id", (_req, res) => {
  res.status(501).json({ message: "Delete routine endpoint pending implementation." });
});
