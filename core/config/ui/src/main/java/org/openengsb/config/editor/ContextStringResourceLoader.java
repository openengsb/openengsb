/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.config.editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A string resource loader to load properties file from the web context. This implementation
 * does not support locales.
 */
public class ContextStringResourceLoader implements IStringResourceLoader {
    private static Logger log = LoggerFactory.getLogger(ContextStringResourceLoader.class);

    private final Map<String,Properties> props;

    public static final ContextStringResourceLoader instance = new ContextStringResourceLoader();

    public ContextStringResourceLoader() {
        this.props = new HashMap<String,Properties>();
    }

    public void addResourceFiles(String key, InputStream stream) {
        Properties p = new Properties();
        try {
            p.load(stream);
            stream.close();
            props.put(key, p);
        } catch (IOException e) {
            log.error("loading component resource file failed for " + key, e);
        }
    }

    public void reset() {
        props.clear();
    }

    @Override
    public String loadStringResource(Component component, String key) {
        if (!Session.exists()) {
            return null;
        }
        return loadStringResource(null, key, Session.get().getLocale(), Session.get().getStyle());
    }

    @Override
    public String loadStringResource(Class<?> clazz, String key, Locale locale, String style) {
        int idx = key.indexOf('.');
        if (idx == -1) {
            return null;
        }
        String first = key.substring(0, idx);
        if (!props.containsKey(first)) {
            return null;
        }
        return props.get(first).getProperty(key.substring(idx + 1));
    }
}
