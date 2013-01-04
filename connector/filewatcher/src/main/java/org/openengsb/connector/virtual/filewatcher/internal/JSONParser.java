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
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.openengsb.connector.virtual.filewatcher.FileSerializer;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class JSONParser<ResultType> implements FileSerializer<ResultType> {

    private final ObjectMapper mapper;

    private abstract class OpenEngSBModelMixin {
        @JsonIgnore
        abstract List<OpenEngSBModelEntry> getOpenEngSBModelTail();
    }

    private Class<ResultType> resultType;

    public JSONParser(Class<ResultType> resultType) {
        this.resultType = resultType;
        mapper = new ObjectMapper();
        mapper.addMixInAnnotations(resultType, OpenEngSBModelMixin.class);
    }

    @Override
    public List<ResultType> readFile(File path) throws IOException {
        if(!path.exists()){
            return Collections.emptyList();
        }
        MappingIterator<ResultType> iterator = mapper.reader(resultType).readValues(path);
        return Lists.newArrayList(iterator);
    }

    @Override
    public void writeFile(File path, List<ResultType> models) throws IOException {
        mapper.writeValue(path, models);
    }
}
