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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.ekb.resources.TMap;
import org.openengsb.ekb.resources.exceptions.NoSuchTMapException;
import org.openengsb.ekb.resources.exceptions.UnableToLoadTMapStorageException;

public class TMapStorage {

    private static Log logger = LogFactory.getLog(TMapStorage.class);

    /**
     * Map<receiverName,Map<senderName,Map<senderMessageType,TMap>>
     */
    private Map<String, Map<String, Map<String, TMap>>> tmapStorage;

    /**
     * create new empty TMap Storage
     */
    public TMapStorage() {
        this.tmapStorage = new HashMap<String, Map<String, Map<String, TMap>>>();
        logger.debug("Created new empty TMap Storage.");
    }

    /**
     * load TMap Storage from URL
     * 
     * @param pathToTMapStorage the path to load the TMap Storage from
     * @throws UnableToLoadTMapStorageException if TMap Storage cannot be loaded
     *         from URL
     */
    public TMapStorage(URL pathToTMapStorage) throws UnableToLoadTMapStorageException {
        this.tmapStorage = this.loadTMapStorage(pathToTMapStorage);
        logger.debug("Succesfully loaded TMap Stroage from: " + pathToTMapStorage + " .");
    }

    /**
     * add TMap to Storage
     * 
     * @param tmap the TMap to add
     */
    public void addTMap(TMap tmap) {
        String receiver = tmap.getReceiver();
        String sender = tmap.getSender();
        String messageType = tmap.getInputMessage().getMessageType();
        if (this.tmapStorage.get(receiver) == null) {
            this.tmapStorage.put(receiver, new HashMap<String, Map<String, TMap>>());
        }
        if (this.tmapStorage.get(receiver).get(sender) == null) {
            this.tmapStorage.get(receiver).put(sender, new HashMap<String, TMap>());
        }
        this.tmapStorage.get(receiver).get(sender).put(messageType, tmap);
    }

    /**
     * retrieve TMap from Storage
     * 
     * @throws NoSuchTMapException if no TMap can be found for given parameters
     */
    public TMap getTMap(String sender, String receiver, String messageType) throws NoSuchTMapException {
        logger.debug(new StringBuilder().append("Looking for TMap between sender '").append(sender).append(
                "' and receiver '").append(receiver).append("' with messageType '").append(messageType).append("'")
                .toString());
        try {
            TMap tmap = this.tmapStorage.get(receiver).get(sender).get(messageType);
            if (tmap != null) {
                logger.debug("Succesfully loaded TMap.");
                return tmap;
            }
        } catch (NullPointerException ex) {
            throw new NoSuchTMapException("Cannot load TMap for sender '" + sender + "' and receiver '" + receiver
                    + "' and messageType '" + messageType + "' !");
        }
        throw new NoSuchTMapException("Cannot load TMap for sender '" + sender + "' and receiver '" + receiver
                + "' and messageType '" + messageType + "' !");
    }

    private Map<String, Map<String, Map<String, TMap>>> loadTMapStorage(URL pathToTMapStorage)
            throws UnableToLoadTMapStorageException {
        return new HashMap<String, Map<String, Map<String, TMap>>>();
    }
}
