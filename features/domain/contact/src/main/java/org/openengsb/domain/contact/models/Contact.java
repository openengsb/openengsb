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

package org.openengsb.domain.contact.models;

import java.util.ArrayList;
import java.util.Date;

/**
 * represents a contact with all their necessary infos
 */
public class Contact {
    private String id;
    private String name;
    private ArrayList<InformationTypeWithValue<String>> mails;
    private ArrayList<InformationTypeWithValue<String>> homepages;
    private ArrayList<InformationTypeWithValue<String>> telephones;
    private ArrayList<InformationTypeWithValue<Location>> locations;
    private ArrayList<InformationTypeWithValue<Date>> dates;
    private String comment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<InformationTypeWithValue<String>> getMails() {
        return mails;
    }

    public void setMails(ArrayList<InformationTypeWithValue<String>> mails) {
        this.mails = mails;
    }

    public ArrayList<InformationTypeWithValue<String>> getHomepages() {
        return homepages;
    }

    public void setHomepages(ArrayList<InformationTypeWithValue<String>> homepages) {
        this.homepages = homepages;
    }

    public ArrayList<InformationTypeWithValue<String>> getTelephones() {
        return telephones;
    }

    public void setTelephones(ArrayList<InformationTypeWithValue<String>> telephones) {
        this.telephones = telephones;
    }

    public ArrayList<InformationTypeWithValue<Location>> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<InformationTypeWithValue<Location>> locations) {
        this.locations = locations;
    }

    public ArrayList<InformationTypeWithValue<Date>> getDates() {
        return dates;
    }

    public void setDates(ArrayList<InformationTypeWithValue<Date>> dates) {
        this.dates = dates;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
