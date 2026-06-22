export function logSyncEvent(event, details = {}) {
  console.info(`[sync] ${event} ${JSON.stringify(details)}`);
}
