/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.persistence;

public class PersistenceInternalExistTest extends PersistenceInternalTest {

    public PersistenceInternalExistTest(Class<?> objectClass, Object o1, Object sample1, Object udpated1) {
        super(objectClass, o1, sample1, udpated1);
    }

    @Override
    protected PersistenceInternal getPersistenceImpl() throws Exception {
        PersistenceInternalExistXmlDB p = new PersistenceInternalExistXmlDB();
        p.reset();
        return p;
    }

}
