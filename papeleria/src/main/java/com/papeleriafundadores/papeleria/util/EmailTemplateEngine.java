package com.papeleriafundadores.papeleria.util;

public class EmailTemplateEngine {

    private EmailTemplateEngine() {
        throw new IllegalStateException("Clase de utilidad");
    }

    // Estructura maestra HTML con los colores corporativos de la papelería
    public static String buildBaseTemplate(String title, String contentHtml) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #F8F9FA; margin: 0; padding: 20px; }
                    .card { max-width: 600px; background: #ffffff; margin: 0 auto; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
                    .header { background-color: #1565C0; padding: 30px; text-align: center; color: white; border-bottom: 5px solid #FFC107; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .body { padding: 40px 30px; color: #212121; line-height: 1.6; }
                    .footer { background-color: #F1F3F4; padding: 20px; text-align: center; color: #616161; font-size: 12px; border-top: 1px solid #E0E0E0; }
                </style>
            </head>
            <body>
                <div class='card'>
                    <div class='header'>
                        <h1>Papelería Fundadores</h1>
                    </div>
                    <div class='body'>
                        <h2 style='color: #1565C0; margin-top: 0;'>%s</h2>
                        %s
                    </div>
                    <div class='footer'>
                        <p>Este es un mensaje automático generado por nuestro sistema, por favor no lo respondas.</p>
                        <p>&copy; 2026 Papelería Fundadores - Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, contentHtml);
    }

    // Plantilla específica para Recuperación de Contraseña
    public static String getResetPasswordTemplate(String name, String url) {
        String content = """
            <p>Hola, <strong>%s</strong>.</p>
            <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta en nuestra plataforma.</p>
            <p>Para continuar con el proceso, haz clic en el siguiente botón seguro (este enlace expira en 15 minutos):</p>
            <div style='text-align: center; margin: 30px 0;'>
                <a href='%s' style='background-color: #1565C0; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>Restablecer Contraseña</a>
            </div>
            <p>Si el botón no funciona, puedes copiar y pegar el siguiente enlace en tu navegador:</p>
            <p style='word-break: break-all; color: #616161; font-size: 13px;'>%s</p>
            <p style='margin-top: 30px;'>Si tú no realizaste esta solicitud, puedes ignorar este correo de forma segura; tu contraseña actual no sufrirá cambios.</p>
            """.formatted(name, url, url);

        return buildBaseTemplate("Recuperación de Contraseña", content);
    }
}
