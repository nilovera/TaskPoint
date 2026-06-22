import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import { randomInt } from "node:crypto";
import { env } from "../config/env.js";
import { sendPasswordResetCode } from "./emailService.js";
import {
  createUser,
  deletePasswordReset,
  deleteUser,
  findUserByEmail,
  findUserById,
  getPasswordReset,
  incrementPasswordResetAttempts,
  markPasswordResetVerified,
  savePasswordReset,
  updateUserPassword
} from "../repositories/userRepository.js";
import { httpError } from "../utils/httpError.js";

const SALT_ROUNDS = 12;
const RESET_CODE_LENGTH = 6;
const RESET_CODE_TTL_MS = 15 * 60 * 1000;
const RESET_VERIFIED_TTL_MS = 10 * 60 * 1000;
const RESET_REQUEST_COOLDOWN_MS = 60 * 1000;
const MAX_RESET_ATTEMPTS = 5;
const RESET_REQUEST_MESSAGE = "Si existe una cuenta con ese correo, recibira un codigo de recuperacion.";

export async function registerUser({ name, email, password }) {
  validateRegisterInput({ name, email, password });

  const existing = await findUserByEmail(email);
  if (existing) {
    throw httpError(409, "El correo ya esta registrado.");
  }

  const passwordHash = await bcrypt.hash(password, SALT_ROUNDS);
  const user = await createUser({
    name: name.trim(),
    email: email.trim(),
    passwordHash
  });

  return createAuthResponse(user);
}

export async function loginUser({ email, password }) {
  if (!email || !password) {
    throw httpError(400, "Email y contrasena son obligatorios.");
  }

  const user = await findUserByEmail(email);
  if (!user || !(await bcrypt.compare(password, user.passwordHash))) {
    throw httpError(401, "Credenciales incorrectas.");
  }

  return createAuthResponse(user);
}

export async function getCurrentUser(userId) {
  const user = await findUserById(userId);
  if (!user) {
    throw httpError(404, "Usuario no encontrado.");
  }
  return sanitizeUser(user);
}

export async function requestPasswordReset(email) {
  if (!email || !isValidEmail(email)) return { message: RESET_REQUEST_MESSAGE };
  const user = await findUserByEmail(email);
  // The response stays generic so this endpoint cannot reveal registered emails.
  if (!user) return { message: RESET_REQUEST_MESSAGE };

  const now = Date.now();
  const previousReset = await getPasswordReset(user.id);
  if (previousReset?.requestedAt && now - previousReset.requestedAt < RESET_REQUEST_COOLDOWN_MS) {
    return { message: RESET_REQUEST_MESSAGE };
  }

  const code = createResetCode();
  await savePasswordReset(user.id, {
    codeHash: await bcrypt.hash(code, SALT_ROUNDS),
    attempts: 0,
    requestedAt: now,
    expiresAt: now + RESET_CODE_TTL_MS,
    verifiedAt: null
  });

  try {
    await sendPasswordResetCode({ email: user.email, code });
  } catch (error) {
    await deletePasswordReset(user.id);
    throw error;
  }

  return { message: RESET_REQUEST_MESSAGE };
}

export async function verifyPasswordResetCode({ email, code }) {
  if (!email || !isValidEmail(email)) throw invalidResetCodeError();
  const user = await findUserByEmail(email);
  const reset = user ? await getPasswordReset(user.id) : null;
  if (!user || !reset) throw invalidResetCodeError();

  const now = Date.now();
  if (reset.expiresAt <= now || reset.attempts >= MAX_RESET_ATTEMPTS) {
    await deletePasswordReset(user.id);
    throw invalidResetCodeError();
  }

  const codeMatches = typeof code === "string" && await bcrypt.compare(code, reset.codeHash);
  if (!codeMatches) {
    await incrementPasswordResetAttempts(user.id);
    throw invalidResetCodeError();
  }

  await markPasswordResetVerified(user.id, now);
  return { message: "Codigo verificado." };
}

export async function resetPassword({ email, newPassword }) {
  validatePassword(newPassword);
  if (!email || !isValidEmail(email)) {
    throw httpError(400, "No se pudo restablecer la contrasena.");
  }

  const user = await findUserByEmail(email);
  if (!user) {
    throw httpError(400, "No se pudo restablecer la contrasena.");
  }

  const reset = await getPasswordReset(user.id);
  const verifiedAt = reset?.verifiedAt;
  if (!verifiedAt || Date.now() - verifiedAt > RESET_VERIFIED_TTL_MS) {
    throw httpError(400, "Verifica un codigo vigente antes de cambiar la contrasena.");
  }

  const passwordHash = await bcrypt.hash(newPassword, SALT_ROUNDS);
  await updateUserPassword(user.id, passwordHash);
  await deletePasswordReset(user.id);

  return { message: "Contrasena actualizada." };
}

export async function changeCurrentPassword(userId, { currentPassword, newPassword }) {
  if (!currentPassword) {
    throw httpError(400, "La contrasena actual es obligatoria.");
  }
  validatePassword(newPassword);

  const user = await findUserById(userId);
  if (!user) {
    throw httpError(404, "Usuario no encontrado.");
  }

  if (!(await bcrypt.compare(currentPassword, user.passwordHash))) {
    throw httpError(401, "La contrasena ingresada es incorrecta.");
  }

  const passwordHash = await bcrypt.hash(newPassword, SALT_ROUNDS);
  await updateUserPassword(user.id, passwordHash);

  return { message: "Contrasena actualizada." };
}

export async function deleteCurrentUser(userId) {
  const user = await findUserById(userId);
  if (!user) {
    throw httpError(404, "Usuario no encontrado.");
  }

  await deleteUser(userId);
  return { message: "Cuenta eliminada." };
}

export function verifyToken(token) {
  try {
    return jwt.verify(token, env.jwt.secret);
  } catch (_error) {
    throw httpError(401, "Token invalido o vencido.");
  }
}

function createAuthResponse(user) {
  return {
    token: jwt.sign(
      { sub: user.id, email: user.email },
      env.jwt.secret,
      { expiresIn: env.jwt.expiresIn }
    ),
    user: sanitizeUser(user)
  };
}

function sanitizeUser(user) {
  return { id: user.id, name: user.name, email: user.email };
}

function validateRegisterInput({ name, email, password }) {
  if (!name || !name.trim()) {
    throw httpError(400, "El nombre es obligatorio.");
  }
  if (!email || !isValidEmail(email)) {
    throw httpError(400, "Ingresa un correo valido.");
  }
  validatePassword(password);
}

function validatePassword(password) {
  if (!password || password.length < 6) {
    throw httpError(400, "La contrasena debe tener al menos 6 caracteres.");
  }
}

function invalidResetCodeError() {
  return httpError(400, "El codigo es invalido o vencio.");
}

function createResetCode() {
  const upperBound = 10 ** RESET_CODE_LENGTH;
  return randomInt(0, upperBound).toString().padStart(RESET_CODE_LENGTH, "0");
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}
