package br.com.oficina.atendimento.interfaces.presenters.view_model;

public record MagicLinkHtmlPageViewModel(String titulo, String corpoHtml) {

    public String html() {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>%s</title>
                  <style>
                    body { font-family: sans-serif; max-width: 32rem; margin: 3rem auto; padding: 0 1rem; color: #1f2937; }
                    main { border: 1px solid #d1d5db; border-radius: 12px; padding: 1.5rem; background: #fff; }
                    h1 { margin-top: 0; font-size: 1.5rem; }
                    button { border: 0; border-radius: 8px; padding: 0.75rem 1rem; background: #111827; color: #fff; cursor: pointer; }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>%s</h1>
                    %s
                  </main>
                </body>
                </html>
                """.formatted(escapeHtml(titulo), escapeHtml(titulo), corpoHtml);
    }

    public static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
