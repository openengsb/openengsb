package org.openengsb.core.common.util;

import java.io.PrintStream;

import org.fusesource.jansi.Ansi;

/**
 * This class is a helper class for the console commands, it prints out the given values to the given stream,
 * default is System.out. It further formats the output using Ansi formats.
 */
public final class OutputStreamFormater {

    private static int padding = 25;
    private static PrintStream outputStream = System.out;

    private OutputStreamFormater() {

    }

    public static void printValue(String name, String value) {
        outputStream.println(formatValues(name, value));
    }

    public static String formatValues(String name, String value) {
        return Ansi.ansi().a("  ").a(Ansi.Attribute.INTENSITY_BOLD).a(name)
            .a(spaces(padding - name.length())).a(Ansi.Attribute.RESET).a("   ").a(value).toString();
    }

    public static String spaces(int nb) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nb; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static void printValue(String value) {
        outputStream.println(value);
    }

    public static void setOutputStream(PrintStream outputStream) {
        OutputStreamFormater.outputStream = outputStream;
    }

    public static void setPadding(int padding) {
        OutputStreamFormater.padding = padding;
    }
}
