import { verifyToken } from "../services/authService.js";
import { httpError } from "../utils/httpError.js";

export function requireAuth(req, _res, next) {
  const header = req.get("Authorization") || "";
  const [scheme, token] = header.split(" ");

  if (scheme !== "Bearer" || !token) {
    return next(httpError(401, "Token requerido."));
  }

  try {
    const payload = verifyToken(token);
    req.user = {
      id: payload.sub,
      email: payload.email
    };
    return next();
  } catch (error) {
    return next(error);
  }
}
