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

/**
 * An abstraction of result set obtained by executing query command. 
 * QueryReader provides methods to iterate (forward-only) over records in 
 * underlying result set and get values for each record column.
 * QueryReader must be closed prior its instance is left for death.
 */
public interface QueryReader {
	
	/**
	 * Advances this reader to the next record. 
	 * 
	 * @return true if there are more records, otherwise false
	 */
	boolean next();
	
	/**
	 * Gets current record column value 
	 * 
	 * @param index column index 
	 * @return value of specified column
	 */
	Object getValue(int index);
	
	/**
	  * Gets current record column value 
	 * 
	 * @param name column name
	 * @return value of specified column
	 */
	Object getValue(String name);
	
	/**
	 * Close this reader. Once this method is called the underlying database connection is released
	 * and underlying result set is disposed. <br>
	 * <b>Method must be called prior this instance is left for garbage collector.</b> 
	 */
	void close();

}
