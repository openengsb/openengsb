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

package org.openengsb.domain.appointment;

import java.util.ArrayList;
import java.util.Date;

import org.openengsb.core.api.Domain;
import org.openengsb.domain.appointment.models.Appointment;

/**
 * This domain is used to integrate different calendar programs. It can create, update, delete and
 * retrieve Appointments.
 */
public interface AppointmentDomain extends Domain {

    /**
     * creates an appointment and returns the generated id
     */
    String createAppointment(Appointment appointment);

    /**
     * updates an appointment
     */
    void updateAppointment(Appointment appointment);

    /**
     * deletes an appointment
     */
    void deleteAppointment(String id);

    /**
     * loads an appointment
     */
    Appointment loadAppointment(String id);
    
    /**
     * Returns a list of appointments which are lying between the given start and end time.
     */
    ArrayList<Appointment> getAppointments(Date start, Date end);
}

