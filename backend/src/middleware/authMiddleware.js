import { findUserById } from "../repositories/userRepository.js";
import { verifyToken } from "../services/authService.js";
import { httpError } from "../utils/httpError.js";

export async function requireAuth(req, _res, next) {
  const header = req.get("Authorization") || "";
  const [scheme, token] = header.split(" ");

  if (scheme !== "Bearer" || !token) {
    return next(httpError(401, "Token requerido."));
  }

  try {
    const payload = verifyToken(token);
    const user = await findUserById(payload.sub);
    if (!user) {
      return next(httpError(401, "Usuario no encontrado o sesion vencida."));
    }
    req.user = {
      id: user.id,
      email: user.email
    };
    return next();
  } catch (error) {
    return next(error);
  }
}
