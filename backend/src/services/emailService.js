import nodemailer from "nodemailer";
import { env } from "../config/env.js";
import { httpError } from "../utils/httpError.js";

export async function sendPasswordResetCode({ email, code }) {
  if (!isEmailConfigured()) {
    throw httpError(503, "El servicio de correo no esta configurado.");
  }

  const transporter = nodemailer.createTransport({
    host: env.email.host,
    port: env.email.port,
    secure: env.email.secure,
    auth: {
      user: env.email.user,
      pass: env.email.password
    }
  });

  await transporter.sendMail({
    from: env.email.from,
    to: email,
    subject: "TaskPoint: codigo para restablecer tu contrasena",
    text: `Tu codigo de recuperacion de TaskPoint es: ${code}. Vence en 15 minutos.`,
    html: `<p>Tu codigo de recuperacion de <strong>TaskPoint</strong> es:</p><p style="font-size: 24px; font-weight: bold; letter-spacing: 4px;">${code}</p><p>Vence en 15 minutos. Si no solicitaste este cambio, ignora este correo.</p>`
  });
}

function isEmailConfigured() {
  return Boolean(
    env.email.host &&
      env.email.user &&
      env.email.password &&
      env.email.from
  );
}
