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

import java.util.List;
import java.util.Map;

public interface Persistence {

    List<Object> query(Object example);

    List<Object> query(List<Object> example);

    void create(Object bean);

    void create(List<Object> beans);

    void update(Object oldBean, Object newBean);

    void update(Map<Object, Object> beans);

    void delete(Object example);

    void delete(List<Object> examples);

}
