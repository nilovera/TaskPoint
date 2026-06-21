import { Router } from "express";

export const referenceRouter = Router();

referenceRouter.get("/categories", (_req, res) => {
  res.status(501).json({ message: "Categories endpoint pending implementation." });
});

referenceRouter.get("/stores", (_req, res) => {
  res.status(501).json({ message: "Stores endpoint pending implementation." });
});

referenceRouter.get("/offers", (_req, res) => {
  res.status(501).json({ message: "Offers endpoint pending implementation." });
});
