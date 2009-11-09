package org.openengsb.context;

class ContextPath {

	private final String path;
	private final String[] elements;

	public ContextPath(String path) {
		this.path = normalizePath(path);
		this.elements = this.path.split("/", -1);
	}

	private String normalizePath(String path) {
		path = path.trim();
		path = path.replaceAll("/+", "/");

		if (path.length() > 0 && path.charAt(0) == '/') {
			path = path.substring(1);
		}

		if (path.length() > 0 && path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}

		return path;
	}

	public String getPath() {
		return path;
	}

	public String[] getElements() {
		return elements;
	}

	public boolean isRoot() {
		return path.isEmpty();
	}
}
