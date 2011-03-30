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

package org.openengsb.connector.gcalendar.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.connector.gcalendar.internal.misc.AppointmentConverter;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.domain.appointment.AppointmentDomain;
import org.openengsb.domain.appointment.models.Appointment;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GcalendarServiceImpl extends AbstractOpenEngSBService implements AppointmentDomain {

    private static Log log = LogFactory.getLog(GcalendarServiceImpl.class);

    private AliveState state = AliveState.DISCONNECTED;
    private String googleUser;
    private String googlePassword;

    private CalendarService service;

    public GcalendarServiceImpl(String id) {
        super(id);
    }

    @Override
    public String createAppointment(Appointment appointment) {
        String id = null;
        try {
            login();
            URL postUrl =
                new URL("https://www.google.com/calendar/feeds/" + googleUser + "/private/full");
            CalendarEventEntry myEntry = AppointmentConverter.convertAppointmentToCalendarEventEntry(appointment);

            // Send the request and receive the response:
            CalendarEventEntry insertedEntry = service.insert(postUrl, myEntry);
            id = insertedEntry.getEditLink().getHref();
            log.info("Successfully created appointment " + id);
            appointment.setId(id);
        } catch (MalformedURLException e) {
            log.error("unknown type of URL");
        } catch (IOException e) {
            log.error("unable to connect to the google server");
        } catch (ServiceException e) {
            log.error("unable to insert the appointment");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
        return id;
    }

    @Override
    public void updateAppointment(Appointment appointment) {
        login();
        CalendarEventEntry entry = getAppointmentEntry(appointment);
        AppointmentConverter.extendCalendarEventEntryWithAppointment(entry, appointment);
        try {
            URL editUrl = new URL(entry.getEditLink().getHref());
            service.update(editUrl, entry);
        } catch (MalformedURLException e) {
            log.error("unknown type of URL");
        } catch (IOException e) {
            log.error("unable to connect to the google server");
        } catch (ServiceException e) {
            log.error("unable to update the appointment");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
    }

    @Override
    public void deleteAppointment(String id) {
        try {
            login();
            Appointment appointment = new Appointment();
            appointment.setId(id);

            CalendarEventEntry entry = getAppointmentEntry(appointment);
            entry.delete();
        } catch (IOException e) {
            log.error("unable to connect to google");
        } catch (ServiceException e) {
            log.error("unable to delete the appointment");
        } finally {
            this.state = AliveState.DISCONNECTED;
        }
    }

    @Override
    public Appointment loadAppointment(String id) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        CalendarEventEntry entry = getAppointmentEntry(appointment);
        return AppointmentConverter.convertCalendarEventEntryToAppointment(entry);
    }

    /**
     * loads an appointment from the server
     */
    private CalendarEventEntry getAppointmentEntry(Appointment appointment) {
        try {
            if (appointment.getId() != null) {
                CalendarEventEntry entry =
                    (CalendarEventEntry) service.getEntry(new URL(appointment.getId()), CalendarEventEntry.class);
                return entry;
            }
        } catch (MalformedURLException e) {
            log.error("unknown type of URL");
        } catch (IOException e) {
            log.error("unable to connect to the google server");
        } catch (ServiceException e) {
            log.error("unable to retrieve the appointment");
        }
        return null;
    }

    /**
     * searches for entries. Every parameter is only taken into concern if not null
     */
    private List<CalendarEventEntry> searchForEntries(Date start, Date end, String text) {
        try {
            URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/private/full");
            CalendarQuery myQuery = new CalendarQuery(feedUrl);
            if (start != null) {
                myQuery.setMinimumStartTime(new DateTime(start.getTime()));
            }
            if (end != null) {
                myQuery.setMaximumStartTime(new DateTime(end.getTime()));
            }
            if (text != null) {
                myQuery.setFullTextQuery(text);
            }
            CalendarEventFeed resultFeed = service.query(myQuery, CalendarEventFeed.class);
            return resultFeed.getEntries();
        } catch (MalformedURLException e) {
            log.error("unknown type of URL");
        } catch (IOException e) {
            log.error("unable to connect to the google server");
        } catch (ServiceException e) {
            log.error("unable to insert the appointment");
        }
        return null;
    }

    @Override
    public ArrayList<Appointment> getAppointments(Date start, Date end) {
        login();
        ArrayList<Appointment> appointments = new ArrayList<Appointment>();

        for (CalendarEventEntry entry : searchForEntries(start, end, null)) {
            Appointment appointment = AppointmentConverter.convertCalendarEventEntryToAppointment(entry);
            appointments.add(appointment);
        }
        this.state = AliveState.DISCONNECTED;

        return appointments;
    }

    @Override
    public AliveState getAliveState() {
        return this.state;
    }

    private void login() {
        try {
            service = new CalendarService("OPENENGSB");
            service.setUserCredentials(googleUser, googlePassword);
            this.state = AliveState.ONLINE;
        } catch (AuthenticationException e) {
            log.error("unable to authenticate at google server, maybe wrong username and/or password?");
        }
    }

    public String getGooglePassword() {
        return googlePassword;
    }

    public void setGooglePassword(String googlePassword) {
        this.googlePassword = googlePassword;
    }

    public String getGoogleUser() {
        return googleUser;
    }

    public void setGoogleUser(String googleUser) {
        this.googleUser = googleUser;
    }
}
