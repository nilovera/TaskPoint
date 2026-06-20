import cors from "cors";
import express from "express";
import { env } from "./config/env.js";
import { errorHandler } from "./middleware/errorHandler.js";
import { notFoundHandler } from "./middleware/notFoundHandler.js";
import { authRouter } from "./routes/authRoutes.js";
import { healthRouter } from "./routes/healthRoutes.js";
import { referenceRouter } from "./routes/referenceRoutes.js";
import { routineRouter } from "./routes/routineRoutes.js";
import { taskRouter } from "./routes/taskRoutes.js";

export function createApp() {
  const app = express();

  app.use(cors());
  app.use(express.json({ limit: "1mb" }));

  app.use("/health", healthRouter);
  app.use("/auth", authRouter);
  app.use("/tasks", taskRouter);
  app.use("/routines", routineRouter);
  app.use("/", referenceRouter);

  app.use(notFoundHandler);
  app.use(errorHandler);

  app.set("appName", env.appName);

  return app;
}
