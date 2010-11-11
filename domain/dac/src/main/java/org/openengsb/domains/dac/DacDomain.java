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

package org.openengsb.domains.dac;

import java.util.List;

import org.openengsb.core.common.Domain;

/**
 * DacDomain is an abstraction of data access component that can access data stored in SQL relational database.
 * DacDomain provides combination of high-level and low-level API approach.
 * <br>
 * <br>1. low-level API is an abstraction of SQL command. 
 * This API consists of following members
 * <li>{@link DacDomain#query(String)} </li>
 * <li>{@link DacDomain#execute(String)} </li>
 * <br>
 * <br>2. high-level API is an abstraction of object mapper. 
 * This API consists of following members
 * <li>{@link DacDomain#getObject(String, Class)} </li>
 * <li>{@link DacDomain#getList(String, Class)} </li>
 * <li>{@link DacDomain#insert(Object)} </li>
 * <li>{@link DacDomain#update(Object)} </li>
 * <li>{@link DacDomain#delete(Object)} </li>
 */
public interface DacDomain extends Domain {

	/* low level approach */

	/**
	 * Executes SQL query command.
	 * 
	 * @param query
	 *            SQL query
	 * @return instance of an {@link QueryReader}
	 */
	QueryReader query(String query);

	/**
	 * Executes non-query (DML) command.
	 * 
	 * @param command
	 *            a DML command to execute
	 * @return count of affected rows
	 */
	int execute(String command);

	/* high level approach */

	/**
	 * Gets single (or first) object from underlying result set.
	 * 
	 * @param query
	 *            SQL query command text
	 * @param type
	 *            of mapped object
	 * @return instance of a mapped object or null
	 */
	Object getObject(String query, Class type);

	/**
	 * Gets list of objects from underlying result set.
	 * 
	 * @param query
	 *            SQL query command text
	 * @param type
	 *            of mapped object
	 * @return list of a mapped objects or empty list
	 */
	List getList(String query, Class type);

	/**
	 * Updates given entity changes to database.
	 * 
	 * @param entity entity to be updated
	 * @return count of updated rows
	 */
	int update(Object entity);

	/**
	 * Inserts newly created entity to database.
	 * 
	 * @param entity entity to be inserted
	 * @return count of inserted rows
	 */
	int insert(Object entity);

	/**
	 * Deletes given entity from database.
	 * 
	 * @param entity entity to be deleted
	 * @return count of deleted rows
	 */
	int delete(Object entity);
}
