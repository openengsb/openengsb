/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.edb.jbi.endpoints.commands;

import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;
import org.openengsb.util.Prelude;

import edu.emory.mathcs.backport.java.util.Arrays;

public class EDBQuery implements EDBEndpointCommand {
	
	private static final String ELEM_OR = " OR ";
	private static final String ELEM_AND = " AND ";
	private static final String ELEM_PATH = "path:";
	private static final String TERM_DELIMITER = ":";
	
	private EDBHandler handler;
	private Log log;
	
	public EDBQuery(EDBHandler handler, Log log) {
		this.handler = handler;
		this.log = log;
	}
	
	@Override
	public String execute(NormalizedMessage in) throws Exception {
		String body = null;
		final List<String> terms = XmlParserFunctions.parseQueryMessage(in);
		List<GenericContent> foundSignals = new ArrayList<GenericContent>();
		log.debug(terms.size() + " queries received, searching now.");
		try {
			for (final String term : terms) {
				final List<GenericContent> result = handler.query(term,
						false);
				foundSignals.addAll(result);
			}

		} catch (final EDBException e) {
			// TODO build error message
			e.printStackTrace();
			foundSignals = new ArrayList<GenericContent>();
		}
		body = XmlParserFunctions.buildQueryBody(foundSignals);
		return body;
	}
	
	public static boolean isNodeQuery(String query) throws ArrayIndexOutOfBoundsException{
		
		if(query.equals("")){
			return false;
		}
		
		// contains OR ?
		if (query.contains(ELEM_OR)) {
			return false;
		}

		// split query by terms
		String[] fieldsArray = query.split(ELEM_AND);		
		//extract term prefix and abstract path
		String path ="";
		List<String> fields = new ArrayList<String>();
		for(int i=0;i<fieldsArray.length;i++) {
			String field = fieldsArray[i];
			//extract path
			if(field.startsWith(ELEM_PATH)){
				path = field.substring(field.indexOf(TERM_DELIMITER) + 1);
			} else {
				// store as element to check against path
				fields.add(field.substring(0, field.indexOf(TERM_DELIMITER)));
			}
		}
		
		// contained "path"
		if(path.equals("")) {
			return false;
		}
		
		// contains last path element ?
		@SuppressWarnings("unchecked")
		List<String> pathNames = new ArrayList<String>(Arrays.asList(Prelude.dePathize(path)));
		if(fields.contains(pathNames.get(pathNames.size() - 1))){
			return false;
		}

		// contains any non-path element ?
		// basic: more elements as fields then path has depth ?
		if(fields.size() >= pathNames.size()) {
			return false; 
		}
		// contains any non-path element ?
		// extended: compare content
		for(int i=0;i<fields.size();i++) {
			String field = fields.get(i);
			if(pathNames.contains(field)) {
				pathNames.remove(field);
				fields.remove(i);
				i--;
			} else {
				return false;
			}
		}
		return true;
	}
	
	public static String printList(List<String> list){
		StringBuffer buf = new StringBuffer();
		for(String obj : list){
			buf.append(obj);
			buf.append(", ");
		}
		return buf.toString();
	}

}
