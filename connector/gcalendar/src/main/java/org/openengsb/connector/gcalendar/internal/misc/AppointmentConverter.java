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

package org.openengsb.connector.gcalendar.internal.misc;

import java.util.Date;
import java.util.TimeZone;

import org.openengsb.domain.appointment.models.Appointment;

import com.google.gdata.data.DateTime;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;

/**
 * does the converting work between the google appointment object (CalendarEventEntry) and the object of the appointment
 * domain
 */
public final class AppointmentConverter {

    private AppointmentConverter() {
    }

    /**
     * converts a calendar event entry to an appointment object
     */
    public static Appointment convertCalendarEventEntryToAppointment(CalendarEventEntry entry) {
        Appointment appointment = new Appointment();
        appointment.setId(entry.getEditLink().getHref());
        // in google multiple Locations can be set
        appointment.setLocation(entry.getLocations().get(0).getLabel());
        appointment.setName(entry.getTitle().getPlainText());
        appointment.setDescription(entry.getPlainTextContent());
        for (When time : entry.getTimes()) {
            if (time.getStartTime().isDateOnly()) {
                appointment.setFullDay(true);
            }
            appointment.setStart(new Date(time.getStartTime().getValue()));
            appointment.setEnd(new Date(time.getEndTime().getValue()));
            break;
        }
        return appointment;
    }

    /**
     * converts an appointment object to a calendar event entry
     */
    public static CalendarEventEntry convertAppointmentToCalendarEventEntry(Appointment appointment) {
        CalendarEventEntry entry = new CalendarEventEntry();
        return extendCalendarEventEntryWithAppointment(entry, appointment);
    }

    /**
     * extends a calendar event entry with information of an appointment
     */
    public static CalendarEventEntry extendCalendarEventEntryWithAppointment(CalendarEventEntry entry,
            Appointment appointment) {
        entry.setId(appointment.getId());

        Where eventLocation = new Where();
        eventLocation.setValueString(appointment.getLocation());
        entry.addLocation(eventLocation);

        entry.setTitle(TextConstruct.plainText(appointment.getName()));
        entry.setContent(TextConstruct.plainText(appointment.getDescription()));

        DateTime startTime = new DateTime(appointment.getStart(), TimeZone.getDefault());
        DateTime endTime = new DateTime(appointment.getEnd(), TimeZone.getDefault());
        When eventTimes = new When();
        eventTimes.setStartTime(startTime);
        eventTimes.setEndTime(endTime);
        entry.addTime(eventTimes);

        return entry;
    }
}
