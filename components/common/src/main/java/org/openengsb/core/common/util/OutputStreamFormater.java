/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public static void printValuesWithPrefix(String pref, String name, String value) {
        outputStream.println(formatValues(pref, name, value));
    }

    public static String formatValues(String name, String value) {
        return Ansi.ansi().a("  ").a(Ansi.Attribute.INTENSITY_BOLD).a(name)
                .a(spaces(padding - name.length())).a(Ansi.Attribute.RESET).a("   ").a(value).toString();
    }

    public static String formatValues(String pref, String name, String value) {
        return Ansi.ansi().a("  ").a("[" + pref + "]").a(Ansi.Attribute.INTENSITY_BOLD).a(name)
                .a(spaces(padding - name.length() - pref.length())).a(Ansi.Attribute.RESET).a("   ").a(value)
                .toString();
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
