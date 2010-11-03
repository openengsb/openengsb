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

package org.openengsb.connector.plaintextreport.internal;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.connector.plaintextreport.internal.PlaintextReportFactory;
import org.openengsb.connector.plaintextreport.internal.PlaintextReportService;
import org.openengsb.domain.report.common.ReportStore;
import org.openengsb.domain.report.common.ReportStoreFactory;

public class PlaintextReportFactoryTest {
    @Test
    public void testCreatePlaintextReportService() throws Exception {
        ReportStoreFactory storeFactory = Mockito.mock(ReportStoreFactory.class);
        ReportStore store = Mockito.mock(ReportStore.class);
        Mockito.when(storeFactory.createReportStore(Mockito.anyString())).thenReturn(store);
        PlaintextReportFactory factory = new PlaintextReportFactory(storeFactory);

        Map<String, String> attributes = new HashMap<String, String>();
        PlaintextReportService reportService = factory.createServiceInstance("id", attributes);

        Assert.assertNotNull(reportService);
        Assert.assertNotNull(reportService.getStore());
        Assert.assertEquals("id", reportService.getId());
    }

}
