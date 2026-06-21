import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import { env } from "../config/env.js";
import {
  createUser,
  deleteUser,
  findUserByEmail,
  findUserById,
  updateUserPassword
} from "../repositories/userRepository.js";
import { httpError } from "../utils/httpError.js";

const SALT_ROUNDS = 12;
const DEV_RESET_CODE = "123456";
const pendingResetEmails = new Set();
const verifiedResetEmails = new Set();

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
    throw httpError(400, "Email y contraseña son obligatorios.");
  }

  const user = await findUserByEmail(email);
  if (!user) {
    throw httpError(401, "Credenciales incorrectas.");
  }

  const passwordMatches = await bcrypt.compare(password, user.passwordHash);
  if (!passwordMatches) {
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
  const user = await findUserByEmail(email);
  if (!user) {
    throw httpError(404, "No existe una cuenta con ese correo.");
  }

  pendingResetEmails.add(normalizeEmail(email));
  console.log(`TaskPoint reset code for ${email}: ${DEV_RESET_CODE}`);

  return { message: "Codigo de recuperacion enviado." };
}

export async function verifyPasswordResetCode({ email, code }) {
  const emailLower = normalizeEmail(email);
  if (!pendingResetEmails.has(emailLower)) {
    throw httpError(400, "Primero solicita el codigo de recuperacion.");
  }

  if (code !== DEV_RESET_CODE) {
    throw httpError(400, "Codigo incorrecto. Intenta de nuevo.");
  }

  pendingResetEmails.delete(emailLower);
  verifiedResetEmails.add(emailLower);
  return { message: "Codigo verificado." };
}

export async function resetPassword({ email, newPassword }) {
  const emailLower = normalizeEmail(email);
  if (!verifiedResetEmails.has(emailLower)) {
    throw httpError(400, "Verifica el codigo antes de cambiar la contraseña.");
  }

  validatePassword(newPassword);

  const user = await findUserByEmail(email);
  if (!user) {
    throw httpError(404, "Usuario no encontrado.");
  }

  const passwordHash = await bcrypt.hash(newPassword, SALT_ROUNDS);
  await updateUserPassword(user.id, passwordHash);
  verifiedResetEmails.delete(emailLower);

  return { message: "Contraseña actualizada." };
}

export async function changeCurrentPassword(userId, { currentPassword, newPassword }) {
  if (!currentPassword) {
    throw httpError(400, "La contraseña actual es obligatoria.");
  }
  validatePassword(newPassword);

  const user = await findUserById(userId);
  if (!user) {
    throw httpError(404, "Usuario no encontrado.");
  }

  const passwordMatches = await bcrypt.compare(currentPassword, user.passwordHash);
  if (!passwordMatches) {
    throw httpError(401, "La contraseña ingresada es incorrecta.");
  }

  const passwordHash = await bcrypt.hash(newPassword, SALT_ROUNDS);
  await updateUserPassword(user.id, passwordHash);

  return { message: "Contraseña actualizada." };
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
      {
        sub: user.id,
        email: user.email
      },
      env.jwt.secret,
      { expiresIn: env.jwt.expiresIn }
    ),
    user: sanitizeUser(user)
  };
}

function sanitizeUser(user) {
  return {
    id: user.id,
    name: user.name,
    email: user.email
  };
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
    throw httpError(400, "La contraseña debe tener al menos 6 caracteres.");
  }
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function normalizeEmail(email) {
  return email.trim().toLowerCase();
}
