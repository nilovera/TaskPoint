import { deleteTaskLastWriteWins, listTaskSyncRecords, listTasks, saveTaskLastWriteWins } from "../repositories/taskRepository.js";
import { httpError } from "../utils/httpError.js";

export function getTasks(userId) {
  return listTasks(userId);
}

export function getTaskSyncRecords(userId) {
  return listTaskSyncRecords(userId);
}

export async function createTask(userId, input) {
  const task = normalizeTask(input);
  return ensureTaskWasNotDeleted(await saveTaskLastWriteWins(userId, task));
}

export async function updateTask(userId, taskId, input) {
  const task = normalizeTask({ ...input, id: taskId });
  if (input?.id && input.id !== taskId) {
    throw httpError(400, "El id del cuerpo no coincide con la ruta.");
  }
  return ensureTaskWasNotDeleted(await saveTaskLastWriteWins(userId, task));
}

export async function removeTask(userId, taskId, input) {
  validateId(taskId);
  const result = await deleteTaskLastWriteWins(
    userId,
    taskId,
    normalizeTimestamp(input?.updatedAt, Date.now())
  );
  if (!result.applied && !result.deleted) {
    throw httpError(409, "Existe una version remota mas nueva de la tarea.");
  }
  return result;
}

function ensureTaskWasNotDeleted(result) {
  if (result.deleted) {
    throw httpError(409, "La tarea fue eliminada en una version remota mas nueva.");
  }
  return result;
}

function normalizeTask(input) {
  const now = Date.now();
  return {
    id: validateId(input?.id),
    titulo: requiredText(input?.titulo, "El titulo es obligatorio."),
    categoriaCode: requiredText(input?.categoriaCode, "La categoria es obligatoria."),
    rutinaId: optionalText(input?.rutinaId),
    rutinaNombre: optionalText(input?.rutinaNombre),
    dia: optionalText(input?.dia),
    horario: optionalText(input?.horario),
    notas: optionalText(input?.notas) ?? "",
    photoPath: optionalText(input?.photoPath),
    completada: Boolean(input?.completada),
    requiereRevisionHorario: Boolean(input?.requiereRevisionHorario),
    createdAt: now,
    updatedAt: normalizeTimestamp(input?.updatedAt, now)
  };
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
