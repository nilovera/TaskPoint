import dotenv from "dotenv";

dotenv.config();

export const env = {
  appName: process.env.APP_NAME || "TaskPoint API",
  nodeEnv: process.env.NODE_ENV || "development",
  port: Number(process.env.PORT || 8080),
  jwt: {
    secret: process.env.JWT_SECRET || "taskpoint-dev-secret",
    expiresIn: process.env.JWT_EXPIRES_IN || "7d"
  },
  firebase: {
    serviceAccountPath: process.env.FIREBASE_SERVICE_ACCOUNT_PATH || "",
    projectId: process.env.FIREBASE_PROJECT_ID || "",
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL || "",
    privateKey: process.env.FIREBASE_PRIVATE_KEY || ""
  }
};
