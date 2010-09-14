/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ekb.runtime.transformation;

import java.net.URL;
import java.util.List;

import org.openengsb.ekb.resources.OpenEngSBMessage;
import org.openengsb.ekb.resources.TMap;
import org.openengsb.ekb.resources.exceptions.NoSuchTMapException;
import org.openengsb.ekb.resources.exceptions.TMapException;
import org.openengsb.ekb.resources.exceptions.UnableToLoadTMapStorageException;


public class TMapProcessor {

    private TMapStorage storage = new TMapStorage();

    public TMapProcessor(List<URL> listOfTMapFiles) throws TMapException {
        this.storage = new TMapStorage();
        for (URL tmapFile : listOfTMapFiles) {
            TMap tmap = new TMap(tmapFile);
            this.storage.addTMap(tmap);
        }
    }

    public TMapProcessor(URL pathToTMapStorage) throws UnableToLoadTMapStorageException {
        this.storage = new TMapStorage(pathToTMapStorage);
    }

    public OpenEngSBMessage transform(OpenEngSBMessage inputMessage) throws NoSuchTMapException, TMapException {
        return this.storage
                .getTMap(inputMessage.getSender(), inputMessage.getReceiver(), inputMessage.getMessageType())
                .performTransformation(inputMessage);
    }
}
