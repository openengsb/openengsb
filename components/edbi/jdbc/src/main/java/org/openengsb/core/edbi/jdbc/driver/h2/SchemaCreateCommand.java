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
package org.openengsb.core.edbi.jdbc.driver.h2;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Idempotent action on a DataSource that creates the necessary underlying schema for the IndexEngine.
 */
public class SchemaCreateCommand {

    private static final String SCHEMA_FILE = "index-schema.h2.sql";

    private DataSource dataSource;

    public SchemaCreateCommand(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates the necessary relations to save Index and IndexField instances.
     */
    public void execute() {
        try {
            new JdbcTemplate(dataSource).execute(readResourceContent(SCHEMA_FILE)); // TODO: sql independence
        } catch (IOException e) {
            throw new RuntimeException("Could not create schema for EDBI Index", e);
        }
    }

    protected String getSchemaResource() {
        return SCHEMA_FILE;
    }

    private String readResourceContent(String resource) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(getSchemaResource())) {
            if (stream == null) {
                throw new IllegalArgumentException("Stream for resource " + resource + " is null");
            }

            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer);
            return writer.toString();
        }
    }

}
