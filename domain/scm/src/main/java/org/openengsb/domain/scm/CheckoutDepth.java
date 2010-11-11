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

package org.openengsb.domain.scm;

/**
 * Checkout depth options. List of available options
 * 
 * <li> <i>EMPTY</i>, see {@link CheckoutDepth#EMPTY}
 * 
 * <li> <i>FILES</i>, see {@link CheckoutDepth#FILES}
 * 
 * <li> <i>DIRS</i>, see {@link CheckoutDepth#DIRS}
 * 
 * <li> <i>ALL</i>, see {@link CheckoutDepth#ALL}
 */
public enum CheckoutDepth  {
	/**
	 *  Checkout only target directory without children files or sub-directories 
	 */
	EMPTY(0), 
	/**
	 *  Checkout target directory and immediate children files
	 */
	FILES(1), 
	/**
	 *  Checkout target directory and immediate children files and sub-directories 
	 */
	DIRS(2), 
	/**
	 *  Checkout target directory and all its children to full recursion 
	 */
	ALL(3);
	
	private int value;
	
	/**
	 * Gets checkout option id
	 * 
	 * @return checkout option id
	 */
	public int getValue() {
		return value;
	}

	private CheckoutDepth(int value){
		this.value = value;
	}
}
