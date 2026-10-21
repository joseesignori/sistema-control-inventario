package com.tienditadonpepe.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Generador mínimo de PDF (solo JDK, sin librerías externas): texto con fuentes estándar Helvetica y Courier,
 * tablas con salto de línea y de página, líneas y rellenos. Suficiente para reportes y comprobantes.
 */
public class PdfDocumento {

    public enum Fuente { NORMAL("F1"), NEGRITA("F2"), MONO("F3");
        final String id;
        Fuente(String id) { this.id = id; }
    }

    public enum Alinear { IZQUIERDA, CENTRO, DERECHA }

    private static final Charset WIN = Charset.forName("windows-1252");

    // Anchos de Helvetica (unidades por 1000 em) para los caracteres ASCII 32..126.
    private static final int[] ANCHO = {
            278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
            1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
            333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
            556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584};

    private final float ancho;
    private final float alto;
    private final float margen;
    private final List<StringBuilder> paginas = new ArrayList<>();
    private StringBuilder actual;
    private float y;
    private boolean pie = true;

    public PdfDocumento(float ancho, float alto, float margen) {
        this.ancho = ancho;
        this.alto = alto;
        this.margen = margen;
        nuevaPagina();
    }

    /** Activa o desactiva el pie «Página x de y» (los tickets no lo llevan). */
    public PdfDocumento conPie(boolean valor) {
        this.pie = valor;
        return this;
    }

    /** Escribe la línea tal cual (conserva los espacios), pensada para texto de ancho fijo. */
    public void lineaLiteral(String texto, Fuente f, float tam) {
        float interlineado = tam * 1.3f;
        asegurarEspacio(interlineado);
        y -= interlineado;
        escribir(limpiar(texto), margen, y + tam * 0.25f, f, tam, NEGRO);
    }

    public static PdfDocumento a4() {
        return new PdfDocumento(595.28f, 841.89f, 42f);
    }

    public float anchoUtil() {
        return ancho - 2 * margen;
    }

    private void nuevaPagina() {
        actual = new StringBuilder();
        paginas.add(actual);
        y = alto - margen;
    }

    private void asegurarEspacio(float necesario) {
        if (y - necesario < margen) nuevaPagina();
    }

    // ---- Medición ------------------------------------------------------------------------

    public static float anchoTexto(String s, Fuente f, float tam) {
        float total = 0;
        for (int i = 0; i < s.length(); i++) total += anchoCaracter(s.charAt(i), f);
        return total * tam / 1000f;
    }

    private static float anchoCaracter(char c, Fuente f) {
        if (f == Fuente.MONO) return 600;
        float w;
        if (c >= 32 && c <= 126) {
            w = ANCHO[c - 32];
        } else {
            String base = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
            char b = base.isEmpty() ? ' ' : base.charAt(0);
            w = (b >= 32 && b <= 126) ? ANCHO[b - 32] : 556;
        }
        return f == Fuente.NEGRITA ? w * 1.06f : w;
    }

    /** Sustituye símbolos que no existen en la codificación estándar del PDF (WinAnsi). */
    static String limpiar(String s) {
        if (s == null) return "";
        return s.replace("≤", "<=").replace("≥", ">=").replace("→", "->").replace("←", "<-").replace("✓", "OK")
                .replace("\u00a0", " ").replace("\t", " ");
    }

    private List<String> ajustar(String texto, Fuente f, float tam, float maxAncho) {
        texto = limpiar(texto);
        List<String> lineas = new ArrayList<>();
        for (String parrafo : texto.split("\n", -1)) {
            StringBuilder linea = new StringBuilder();
            for (String palabra : parrafo.split(" ")) {
                String candidato = linea.length() == 0 ? palabra : linea + " " + palabra;
                if (anchoTexto(candidato, f, tam) <= maxAncho || linea.length() == 0) {
                    linea.setLength(0);
                    linea.append(candidato);
                    while (anchoTexto(linea.toString(), f, tam) > maxAncho && linea.length() > 1) {
                        // palabra única demasiado larga: se corta
                        int corte = linea.length() - 1;
                        while (corte > 1 && anchoTexto(linea.substring(0, corte), f, tam) > maxAncho) corte--;
                        lineas.add(linea.substring(0, corte));
                        String resto = linea.substring(corte);
                        linea.setLength(0);
                        linea.append(resto);
                    }
                } else {
                    lineas.add(linea.toString());
                    linea.setLength(0);
                    linea.append(palabra);
                }
            }
            lineas.add(linea.toString());
        }
        return lineas;
    }

    // ---- Dibujo ----------------------------------------------------------------------------

    private void escribir(String texto, float x, float baseY, Fuente f, float tam, float[] color) {
        actual.append("BT ").append(f(color[0])).append(' ').append(f(color[1])).append(' ').append(f(color[2])).append(" rg /")
                .append(f.id).append(' ').append(f(tam)).append(" Tf ").append(f(x)).append(' ').append(f(baseY)).append(" Td (")
                .append(escapar(texto)).append(") Tj ET\n");
    }

    private static final float[] NEGRO = {0f, 0f, 0f};

    /** Párrafo ajustado al ancho útil. */
    public void texto(String texto, Fuente f, float tam, Alinear alinear, float[] color) {
        float interlineado = tam * 1.3f;
        for (String linea : ajustar(texto, f, tam, anchoUtil())) {
            asegurarEspacio(interlineado);
            y -= interlineado;
            float w = anchoTexto(linea, f, tam);
            float x = margen;
            if (alinear == Alinear.CENTRO) x = margen + (anchoUtil() - w) / 2;
            else if (alinear == Alinear.DERECHA) x = margen + anchoUtil() - w;
            escribir(linea, x, y + tam * 0.25f, f, tam, color == null ? NEGRO : color);
        }
    }

    public void texto(String texto, Fuente f, float tam, Alinear alinear) {
        texto(texto, f, tam, alinear, null);
    }

    public void texto(String texto, Fuente f, float tam) {
        texto(texto, f, tam, Alinear.IZQUIERDA, null);
    }

    /** Fila de dos columnas: etiqueta a la izquierda y valor a la derecha. */
    public void parEtiquetaValor(String etiqueta, String valor, Fuente f, float tam) {
        float interlineado = tam * 1.3f;
        asegurarEspacio(interlineado);
        y -= interlineado;
        etiqueta = limpiar(etiqueta);
        valor = limpiar(valor);
        escribir(etiqueta, margen, y + tam * 0.25f, f, tam, NEGRO);
        float w = anchoTexto(valor, f, tam);
        escribir(valor, margen + anchoUtil() - w, y + tam * 0.25f, f, tam, NEGRO);
    }

    public void espacio(float puntos) {
        y -= puntos;
        if (y < margen) nuevaPagina();
    }

    public void linea(float grosor, float[] color) {
        asegurarEspacio(grosor + 2);
        y -= 1;
        actual.append(f(color[0])).append(' ').append(f(color[1])).append(' ').append(f(color[2])).append(" RG ")
                .append(f(grosor)).append(" w ").append(f(margen)).append(' ').append(f(y)).append(" m ")
                .append(f(margen + anchoUtil())).append(' ').append(f(y)).append(" l S\n");
        y -= 1;
    }

    public void lineaPunteada() {
        asegurarEspacio(4);
        y -= 2;
        actual.append("0.4 0.4 0.4 RG 0.6 w [2 2] 0 d ").append(f(margen)).append(' ').append(f(y)).append(" m ")
                .append(f(margen + anchoUtil())).append(' ').append(f(y)).append(" l S [] 0 d\n");
        y -= 2;
    }

    /**
     * Tabla con encabezado repetido en cada página. {@code pesos} reparte el ancho útil; las celdas se ajustan en varias líneas.
     */
    public void tabla(String[] encabezados, float[] pesos, Alinear[] alineaciones, List<String[]> filas, float tam) {
        float suma = 0;
        for (float p : pesos) suma += p;
        float[] anchos = new float[pesos.length];
        for (int i = 0; i < pesos.length; i++) anchos[i] = anchoUtil() * pesos[i] / suma;
        float pad = 4f;
        float interlineado = tam * 1.25f;

        dibujarFilaTabla(encabezados, anchos, alineaciones, Fuente.NEGRITA, tam, pad, interlineado, true, false);
        int n = 0;
        for (String[] fila : filas) {
            if (y - interlineado - 2 * pad < margen) {
                nuevaPagina();
                dibujarFilaTabla(encabezados, anchos, alineaciones, Fuente.NEGRITA, tam, pad, interlineado, true, false);
            }
            dibujarFilaTabla(fila, anchos, alineaciones, Fuente.NORMAL, tam, pad, interlineado, false, n++ % 2 == 1);
        }
    }

    private void dibujarFilaTabla(String[] celdas, float[] anchos, Alinear[] al, Fuente f, float tam, float pad,
                                  float interlineado, boolean encabezado, boolean sombreado) {
        List<List<String>> lineasPorCelda = new ArrayList<>();
        int maxLineas = 1;
        for (int i = 0; i < celdas.length; i++) {
            List<String> ls = ajustar(celdas[i] == null ? "" : celdas[i], f, tam, anchos[i] - 2 * pad);
            lineasPorCelda.add(ls);
            maxLineas = Math.max(maxLineas, ls.size());
        }
        float altoFila = maxLineas * interlineado + 2 * pad;
        asegurarEspacio(altoFila);
        float top = y;
        float bottom = y - altoFila;
        if (encabezado) {
            actual.append("0.12 0.30 0.45 rg ").append(f(margen)).append(' ').append(f(bottom)).append(' ')
                    .append(f(anchoUtil())).append(' ').append(f(altoFila)).append(" re f\n");
        } else if (sombreado) {
            actual.append("0.95 0.96 0.97 rg ").append(f(margen)).append(' ').append(f(bottom)).append(' ')
                    .append(f(anchoUtil())).append(' ').append(f(altoFila)).append(" re f\n");
        }
        float[] color = encabezado ? new float[]{1f, 1f, 1f} : NEGRO;
        float x = margen;
        for (int i = 0; i < celdas.length; i++) {
            float ty = top - pad - tam;
            for (String linea : lineasPorCelda.get(i)) {
                float w = anchoTexto(linea, f, tam);
                float tx = x + pad;
                Alinear a = al == null || i >= al.length ? Alinear.IZQUIERDA : al[i];
                if (a == Alinear.DERECHA) tx = x + anchos[i] - pad - w;
                else if (a == Alinear.CENTRO) tx = x + (anchos[i] - w) / 2;
                escribir(linea, tx, ty + tam * 0.2f, f, tam, color);
                ty -= interlineado;
            }
            x += anchos[i];
        }
        actual.append("0.80 0.83 0.86 RG 0.4 w ").append(f(margen)).append(' ').append(f(bottom)).append(" m ")
                .append(f(margen + anchoUtil())).append(' ').append(f(bottom)).append(" l S\n");
        y = bottom;
    }

    // ---- Salida ----------------------------------------------------------------------------

    public void guardar(Path destino) throws IOException {
        Files.write(destino, generar());
    }

    public byte[] generar() throws IOException {
        // Pie con numeración
        int total = paginas.size();
        for (int i = 0; pie && i < total; i++) {
            StringBuilder p = paginas.get(i);
            String pie = "Página " + (i + 1) + " de " + total;
            float w = anchoTexto(pie, Fuente.NORMAL, 8);
            p.append("BT 0.45 0.45 0.45 rg /F1 8 Tf ").append(f(margen + anchoUtil() - w)).append(' ').append(f(margen - 20))
                    .append(" Td (").append(escapar(pie)).append(") Tj ET\n");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        escribirCrudo(out, "%PDF-1.4\n%âãÏÓ\n");
        // Objetos: 1 catálogo, 2 páginas, 3-5 fuentes, luego pares página/contenido
        int primerPagina = 6;
        offsets.add(out.size());
        escribirCrudo(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        offsets.add(out.size());
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < total; i++) kids.append(primerPagina + 2 * i).append(" 0 R ");
        escribirCrudo(out, "2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count " + total + " >>\nendobj\n");
        String[] fuentes = {"Helvetica", "Helvetica-Bold", "Courier"};
        for (int i = 0; i < 3; i++) {
            offsets.add(out.size());
            escribirCrudo(out, (3 + i) + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /" + fuentes[i]
                    + " /Encoding /WinAnsiEncoding >>\nendobj\n");
        }
        for (int i = 0; i < total; i++) {
            int idPagina = primerPagina + 2 * i;
            offsets.add(out.size());
            escribirCrudo(out, idPagina + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + f(ancho) + " " + f(alto)
                    + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R /F3 5 0 R >> >> /Contents " + (idPagina + 1) + " 0 R >>\nendobj\n");
            byte[] contenido = paginas.get(i).toString().getBytes(WIN);
            offsets.add(out.size());
            escribirCrudo(out, (idPagina + 1) + " 0 obj\n<< /Length " + contenido.length + " >>\nstream\n");
            out.write(contenido);
            escribirCrudo(out, "\nendstream\nendobj\n");
        }
        int xref = out.size();
        int objetos = offsets.size() + 1;
        StringBuilder x = new StringBuilder("xref\n0 " + objetos + "\n0000000000 65535 f \n");
        for (int off : offsets) x.append(String.format(Locale.US, "%010d 00000 n \n", off));
        x.append("trailer\n<< /Size ").append(objetos).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF\n");
        escribirCrudo(out, x.toString());
        return out.toByteArray();
    }

    private static void escribirCrudo(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
    }

    private static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == ')' || c == '\\') sb.append('\\').append(c);
            else if (c == '\n' || c == '\r') sb.append(' ');
            else if (c < 128) sb.append(c);
            else {
                byte[] b = String.valueOf(c).getBytes(WIN);
                int v = b[0] & 0xFF;
                if (v == '?' && c != '?') v = '?';
                sb.append('\\').append(String.format("%03o", v));
            }
        }
        return sb.toString();
    }

    private static String f(float v) {
        return String.format(Locale.US, "%.2f", v);
    }
}
