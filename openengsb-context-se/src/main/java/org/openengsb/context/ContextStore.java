package org.openengsb.context;

public class ContextStore {
	private Context rootContext = new Context();

	public Context getContext(String path) {
		path = normalizePath(path);
		return new Context(resolve(path));
	}

	public Context getContext(String path, int depth) {
		if (depth <= 0) {
			throw new IllegalArgumentException("Depth must be positive");
		}
		Context context = getContext(path);
		prune(context, depth, 1);
		return context;
	}

	private void prune(Context ctx, int depth, int currentDepth) {
		if (currentDepth >= depth) {
			for (String child : ctx.getChildrenNames()) {
				ctx.removeChild(child);
			}
			return;
		}

		for (String child : ctx.getChildrenNames()) {
			prune(ctx.getChild(child), depth, currentDepth + 1);
		}
	}

	public void setValue(String path, String value) {
		path = normalizePath(path);
		String[] splitPath = splitPath(path);
		Context ctx = resolveAndCreate(splitPath[0]);
		ctx.set(splitPath[1], value);
	}

	private String[] splitPath(String path) {
		String[] s = new String[2];
		int index = path.lastIndexOf('/');

		if (index == -1) {
			s[0] = "";
			s[1] = path;
		} else {
			s[0] = path.substring(0, index);
			s[1] = path.substring(index + 1);
		}

		return s;
	}

	private Context resolveAndCreate(String path) {
		return resolve(path, true);
	}

	private Context resolve(String path) {
		return resolve(path, false);
	}

	private String normalizePath(String path) {
		path = path.replaceAll("/+", "/");

		if (path.length() > 0 && path.charAt(0) == '/') {
			path = path.substring(1);
		}

		if (path.length() > 0 && path.charAt(path.length() - 1) == '/') {
			path = path.substring(0, path.length() - 1);
		}

		return path;
	}

	private Context resolve(String path, boolean create) {
		if (path.isEmpty()) {
			return rootContext;
		}

		String[] split = path.split("/", -1);

		Context ctx = rootContext;
		Context last;

		for (int i = 0; i < split.length; i++) {
			String pathElement = split[i];

			last = ctx;
			ctx = ctx.getChild(pathElement);

			if (ctx == null) {
				if (!create) {
					throw new ContextNotFoundException("Can't find context "
							+ path);
				}

				last.createChild(pathElement);
				ctx = last.getChild(pathElement);
			}
		}

		return ctx;
	}

	private void load() {
		// TODO
	}

	private void save() {
		// TODO
	}
}
