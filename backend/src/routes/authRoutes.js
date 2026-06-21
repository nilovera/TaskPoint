import { Router } from "express";
import { requireAuth } from "../middleware/authMiddleware.js";
import {
  changeCurrentPassword,
  deleteCurrentUser,
  getCurrentUser,
  loginUser,
  registerUser,
  requestPasswordReset,
  resetPassword,
  verifyPasswordResetCode
} from "../services/authService.js";
import { asyncHandler } from "../utils/asyncHandler.js";

export const authRouter = Router();

authRouter.post(
  "/register",
  asyncHandler(async (req, res) => {
    const response = await registerUser(req.body);
    res.status(201).json(response);
  })
);

authRouter.post(
  "/login",
  asyncHandler(async (req, res) => {
    const response = await loginUser(req.body);
    res.json(response);
  })
);

authRouter.get(
  "/me",
  requireAuth,
  asyncHandler(async (req, res) => {
    const user = await getCurrentUser(req.user.id);
    res.json(user);
  })
);

authRouter.post(
  "/password/reset-code",
  asyncHandler(async (req, res) => {
    const response = await requestPasswordReset(req.body.email);
    res.json(response);
  })
);

authRouter.post(
  "/password/verify-code",
  asyncHandler(async (req, res) => {
    const response = await verifyPasswordResetCode(req.body);
    res.json(response);
  })
);

authRouter.post(
  "/password/reset",
  asyncHandler(async (req, res) => {
    const response = await resetPassword(req.body);
    res.json(response);
  })
);

authRouter.put(
  "/me/password",
  requireAuth,
  asyncHandler(async (req, res) => {
    const response = await changeCurrentPassword(req.user.id, req.body);
    res.json(response);
  })
);

authRouter.delete(
  "/me",
  requireAuth,
  asyncHandler(async (req, res) => {
    const response = await deleteCurrentUser(req.user.id);
    res.json(response);
  })
);
