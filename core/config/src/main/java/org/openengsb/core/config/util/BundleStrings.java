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
package org.openengsb.core.config.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.LocaleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

/**
 * Helper class to handle the locale files provided by a {@code Bundle}.
 */
public class BundleStrings {

    private Bundle bundle;
    private String directory;
    private String basename;

    public BundleStrings() {
    }

    public BundleStrings(Bundle bundle) {
        setBundle(bundle);
    }

    public String getString(String key) {
        return getString(key, Locale.getDefault());
    }

    public String getString(String key, Locale locale) {
        @SuppressWarnings("unchecked")
        Enumeration<URL> entries = bundle.findEntries(directory, basename + "*.properties", false);
        if (entries == null || !entries.hasMoreElements()) {
            return null;
        }
        HashMap<String, URL> map = new HashMap<String, URL>();
        while (entries.hasMoreElements()) {
            URL url = entries.nextElement();
            String name = new File(url.toString()).getName();
            map.put(name, url);
        }
        for (Locale l : (List<Locale>) LocaleUtils.localeLookupList(locale, new Locale(""))) {
            String name = basename;
            if (!l.getLanguage().isEmpty()) {
                name += '_' + l.getLanguage();
                if (!l.getCountry().isEmpty()) {
                    name += '_' + l.getCountry();
                    if (!l.getVariant().isEmpty()) {
                        name += '_' + l.getVariant();
                    }
                }
            }
            name += ".properties";
            System.out.println(name);
            URL url = map.get(name);
            if (url == null) {
                continue;
            } else {
                InputStream in = null;
                try {
                    in = url.openStream();
                    Properties p = new Properties();
                    p.load(in);
                    if (p.containsKey(key)) {
                        return p.getProperty(key);
                    }
                } catch (IOException e) {
                    // TODO
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // no op
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
        String path = (String) bundle.getHeaders().get(Constants.BUNDLE_LOCALIZATION);
        if (path == null) {
            path = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
        }
        path = path.trim();
        int idx = path.lastIndexOf('/');
        if (idx == -1) {
            directory = "/";
            basename = path;
        } else if (idx == 0) {
            directory = "/";
            basename = path.substring(1);
        } else {
            directory = path.substring(0, idx);
            basename = path.substring(idx + 1);
        }
    }
}
