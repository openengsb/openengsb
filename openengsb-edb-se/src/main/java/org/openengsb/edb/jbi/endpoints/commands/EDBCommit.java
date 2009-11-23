package org.openengsb.edb.jbi.endpoints.commands;

import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.entities.OperationType;
import org.openengsb.edb.jbi.endpoints.EdbEndpoint;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions.ContentWrapper;

public class EDBCommit implements EDBEndpointCommand {
	
	private EDBHandler handler;
	private Log log;
	
	public EDBCommit(EDBHandler handler, Log log) {
		this.handler = handler;
		this.log = log;
	}
	
	@Override
	public String execute(NormalizedMessage in) throws Exception {
		String body = null;
		try {
            List<ContentWrapper> contentWrappers = XmlParserFunctions.parseCommitMessage(in, handler
                    .getRepositoryBase().toString());
            if (contentWrappers.size() < 1) {
                throw new EDBException("Message did not contain files to commit");
            }
            final List<GenericContent> listAdd = new ArrayList<GenericContent>();
            final List<GenericContent> listRemove = new ArrayList<GenericContent>();

            for (final ContentWrapper content : contentWrappers) {
                // update search index
                if (content.getOperation() == OperationType.UPDATE) {
                    listAdd.add(content.getContent());
                } else if (content.getOperation() == OperationType.DELETE) {
                    listRemove.add(content.getContent());
                }
            }

            handler.add(listAdd);
            handler.remove(listRemove);

            String commitId = handler.commit(EdbEndpoint.DEFAULT_USER, EdbEndpoint.DEFAULT_EMAIL);
            body = XmlParserFunctions.buildCommitBody(contentWrappers, commitId);
        } catch (EDBException e) {
            body = XmlParserFunctions.buildCommitErrorBody(e.getMessage(), makeStackTraceString(e));
            this.log.info(body);
        }
        return body;
	}
	
	private String makeStackTraceString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
