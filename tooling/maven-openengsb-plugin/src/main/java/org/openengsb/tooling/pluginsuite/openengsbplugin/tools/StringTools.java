package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

public abstract class StringTools {

	public static String capitalizeFirst(String st) {
		if (st == null) {
			return null;
		} else if (st.matches("[\\s]*")) {
			return st;
		} else if (st.length() == 1) {
			return st.toUpperCase();
		} else {
			return st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
		}
	}

}
