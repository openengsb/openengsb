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
package org.openengsb.connector.virtual.filewatcher.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.openengsb.connector.virtual.filewatcher.FileSerializer;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Lists;

public class CSVParser<ResultType> implements FileSerializer<ResultType> {

    private abstract class OpenEngSBModelMixin {
        @JsonIgnore
        abstract List<OpenEngSBModelEntry> getOpenEngSBModelTail();
    }

    private Class<ResultType> resultType;

    public CSVParser(Class<ResultType> resultType) {
        this.resultType = resultType;
    }

    @Override
    public List<ResultType> readFile(File path) throws IOException {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(resultType);
        try {
            MappingIterator<ResultType> iterator = csvMapper.reader(schema).withType(resultType)
                .readValues(path);
            return Lists.newArrayList(iterator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeFile(File path, List<ResultType> models) throws IOException {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.addMixInAnnotations(resultType, OpenEngSBModelMixin.class);
        CsvSchema schema = csvMapper.schemaFor(resultType);
        System.out.println(schema.getColumnDesc());
        ObjectWriter writer = csvMapper.writer(schema);
        // supplying the file directly does not work.
        // java.lang.UnsupportedOperationException: Generator of type com.fasterxml.jackson.core.json.UTF8JsonGenerator
        // does not support schema of type 'CSV'
        writer.writeValue(new FileOutputStream(path), models);
    }
}
