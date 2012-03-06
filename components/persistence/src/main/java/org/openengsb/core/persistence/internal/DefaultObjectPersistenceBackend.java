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

package org.openengsb.core.persistence.internal;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.SpecialActionsAfterSerialisation;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default file reader/writer implementation
 */
public class DefaultObjectPersistenceBackend implements ObjectPersistenceBackend {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultObjectPersistenceBackend.class);
    private Bundle bundle;

    public DefaultObjectPersistenceBackend() {
    }

    public DefaultObjectPersistenceBackend(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public void writeDatabaseObject(Object obj, File file) throws PersistenceException {
        LOGGER.trace("Trying to serialize object {} to file {}", obj.getClass().getName(), file.toString());
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(file);
            out = new ObjectOutputStream(fos);
            try {
                out.writeObject(obj);
            } catch (NotSerializableException e) {
                new SerializableChecker(e).writeObject(obj);
            }
        } catch (IOException e) {
            throw new PersistenceException(format("Could not write object %s to file %s", obj.getClass().getName(),
                file.toString()), e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LOGGER.debug(
                    format("Could not close ObjectOutputStream for object %s and file %s", obj.getClass().getName(),
                        file.toString()), e);
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOGGER.debug(
                    format("Could not close FileOutputStream for object %s and file %s", obj.getClass().getName(),
                        file.toString()), e);
            }
        }
    }

    @Override
    public Object readDatabaseObject(final File file) throws PersistenceException {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(file);
            in = new ObjectInputStream(fis) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    LOGGER.debug(format("Trying to load class %s for file %s from current ContextClassLoader",
                        desc.getName(), file.toString()));
                    try {
                        return Thread.currentThread().getContextClassLoader().loadClass(desc.getName());
                    } catch (Throwable e) {
                        LOGGER.debug(format("Couldn't load class %s for file %s from current ContextClassLoader",
                            desc.getName(), file.toString()));
                    }
                    if (bundle != null) {
                        LOGGER.debug(format("Trying to load class %s for file %s from bundle %s", desc.getName(),
                            file.toString(), bundle.getSymbolicName()));
                        try {
                            Class<?> loadedClass = bundle.loadClass(desc.getName());
                            if (loadedClass != null) {
                                return loadedClass;
                            } else {
                                LOGGER.debug(format("Couldn't load class %s for file %s from bundle %s",
                                    desc.getName(), file.toString(), bundle.getSymbolicName()));
                            }
                        } catch (Throwable e) {
                            LOGGER.debug(format("Couldn't load class %s for file %s from bundle %s", desc.getName(),
                                file.toString(), bundle.getSymbolicName()));
                        }
                    }
                    LOGGER.debug(format("Every alternative failed; try to load class %s for file %s from parent.",
                        desc.getName(), file.toString()));
                    return super.resolveClass(desc);
                }
            };
            Object object = in.readObject();
            if (object instanceof SpecialActionsAfterSerialisation) {
                ((SpecialActionsAfterSerialisation) object).doSpecialActions();
            }
            return object;
        } catch (IOException e) {
            throw new PersistenceException(format("Could not read file %s", file.toString()), e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(format("Could not load required classes for file %s", file.toString()), e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOGGER.debug(format("Could not close ObjectInputStream for file %s", file.toString()), e);
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                LOGGER.debug(format("Could not close FileInputStream for file %s", file.toString()), e);
            }
        }
    }

}
