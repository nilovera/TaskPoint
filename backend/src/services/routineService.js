import { deleteRoutine, listRoutines, saveRoutineLastWriteWins } from "../repositories/routineRepository.js";
import { httpError } from "../utils/httpError.js";

export function getRoutines(userId) {
  return listRoutines(userId);
}

export async function createRoutine(userId, input) {
  const routine = normalizeRoutine(input);
  return saveRoutineLastWriteWins(userId, routine);
}

export async function updateRoutine(userId, routineId, input) {
  const routine = normalizeRoutine({ ...input, id: routineId });
  if (input?.id && input.id !== routineId) {
    throw httpError(400, "El id del cuerpo no coincide con la ruta.");
  }
  return (await saveRoutineLastWriteWins(userId, routine)).routine;
}

export async function removeRoutine(userId, routineId) {
  validateId(routineId);
  const deleted = await deleteRoutine(userId, routineId);
  if (!deleted) throw httpError(404, "Rutina no encontrada.");
}

function normalizeRoutine(input) {
  const now = Date.now();
  return {
    id: validateId(input?.id),
    nombre: requiredText(input?.nombre, "El nombre es obligatorio."),
    icono: requiredText(input?.icono, "El icono es obligatorio."),
    direccion: requiredText(input?.direccion, "La direccion es obligatoria."),
    diasSemana: normalizeDays(input?.diasSemana),
    horarioInicio: requiredText(input?.horarioInicio, "El horario de inicio es obligatorio."),
    horarioFin: requiredText(input?.horarioFin, "El horario de fin es obligatorio."),
    descripcion: requiredText(input?.descripcion, "La descripcion es obligatoria."),
    createdAt: now,
    updatedAt: normalizeTimestamp(input?.updatedAt, now)
  };
}

function normalizeDays(value) {
  if (!Array.isArray(value) || value.length === 0 || value.some(day => typeof day !== "string" || !day.trim())) {
    throw httpError(400, "Debes indicar al menos un dia.");
  }
  return [...new Set(value.map(day => day.trim()))];
}

function validateId(value) {
  const id = requiredText(value, "El id es obligatorio.");
  if (id.includes("/") || id.length > 128) {
    throw httpError(400, "El id no es valido.");
  }
  return id;
}

function requiredText(value, message) {
  const text = optionalText(value);
  if (!text) throw httpError(400, message);
  return text;
}

function optionalText(value) {
  if (value === null || value === undefined) return null;
  if (typeof value !== "string") throw httpError(400, "El formato de datos no es valido.");
  return value.trim() || null;
}

function normalizeTimestamp(value, fallback) {
  if (value === null || value === undefined) return fallback;
  if (!Number.isSafeInteger(value) || value < 0) {
    throw httpError(400, "updatedAt no es valido.");
  }
  return value;
}
