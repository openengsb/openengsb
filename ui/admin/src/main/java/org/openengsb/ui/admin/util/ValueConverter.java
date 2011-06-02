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

package org.openengsb.ui.admin.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

import org.apache.wicket.util.convert.converters.AbstractConverter;
import org.apache.wicket.util.convert.converters.IntegerConverter;
import org.apache.wicket.util.convert.converters.LongConverter;

@SuppressWarnings("rawtypes")
public class ValueConverter implements Serializable {

    private static final long serialVersionUID = 7243035341677925260L;

    private final HashMap<Class<?>, AbstractConverter> converters = new HashMap<Class<?>, AbstractConverter>();

    public ValueConverter() {
        converters.put(Integer.class, new IntegerConverter());
        converters.put(Long.class, new LongConverter());
    }

    @SuppressWarnings("unchecked")
    public Object convert(Class<?> type, String object) {
        AbstractConverter abstractConverter;
        if (Enum.class.isAssignableFrom(type)) {
            abstractConverter = new EnumConverter((Class<? extends Enum>) type);
        } else {
            abstractConverter = converters.get(type);
        }
        if (abstractConverter != null) {
            return abstractConverter.convertToObject(object, Locale.getDefault());
        } else {
            return object;
        }
    }

    public class EnumConverter extends AbstractConverter {
        private static final long serialVersionUID = -3882902766643241025L;

        private final Class<? extends Enum> type;

        public EnumConverter(Class<? extends Enum> type) {
            this.type = type;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object convertToObject(String value, Locale locale) {
            return Enum.valueOf(type, value);
        }

        @Override
        protected Class<?> getTargetType() {
            return type;
        }
    }
}
