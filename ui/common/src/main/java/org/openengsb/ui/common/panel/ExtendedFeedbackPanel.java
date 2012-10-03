/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.common.panel;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessagesModel;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class ExtendedFeedbackPanel extends Panel implements IFeedback
{
	/**
	 * List for messages.
	 */
	private final class MessageListView extends ListView<FeedbackMessage>
	{
		private static final long serialVersionUID = 1L;

		private MessageType messageType = MessageType.ERROR;
		
		public MessageListView(final String id)
		{
			super(id);
			setDefaultModel(newFeedbackMessagesModel());
		}
		
		public MessageListView(final String id, MessageType messageType)
		{
			super(id);
			setDefaultModel(newFeedbackMessagesModel());
			this.messageType = messageType;
		}

		@Override
		protected IModel<FeedbackMessage> getListItemModel(
			final IModel<? extends List<FeedbackMessage>> listViewModel, final int index)
		{
			return new AbstractReadOnlyModel<FeedbackMessage>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public FeedbackMessage getObject()
				{
					if (index >= listViewModel.getObject().size())
					{
						return null;
					}
					else
					{
						return listViewModel.getObject().get(index);
					}
				}
			};
		}

		@Override
		protected void populateItem(final ListItem<FeedbackMessage> listItem)
		{
			final IModel<String> replacementModel = new Model<String>()
			{
				private static final long serialVersionUID = 1L;

				/**
				 * Returns feedbackPanel + the message level, eg 'feedbackPanelERROR'. This is used
				 * as the class of the li / span elements.
				 * 
				 * @see org.apache.wicket.model.IModel#getObject()
				 */
				@Override
				public String getObject()
				{
					return getCSSClass(listItem.getModelObject());
				}
			};

			final FeedbackMessage message = listItem.getModelObject();
			if(message.isInfo() && !messageType.equals(MessageType.INFO))
				listItem.setVisible(false);
			else if(message.isWarning() && !messageType.equals(MessageType.WARNING))
				listItem.setVisible(false);
			else if(message.isError() && !messageType.equals(MessageType.ERROR))
				listItem.setVisible(false);
			else
				message.markRendered();
			final Component label = newMessageDisplayComponent("message", message);
			final AttributeModifier levelModifier = new AttributeModifier("class", replacementModel);
			label.add(levelModifier);
			listItem.add(levelModifier);
			listItem.add(label);
		}
	}

	private static final long serialVersionUID = 1L;

	/** Message view */
	private final MessageListView errorMessageListView;
	private final MessageListView warningMessageListView;
	private final MessageListView infoMessageListView;

	
	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public ExtendedFeedbackPanel(final String id)
	{
		this(id, null);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 * 
	 * @param id
	 * @param filter
	 */
	public ExtendedFeedbackPanel(final String id, IFeedbackMessageFilter filter)
	{
		super(id);
		WebMarkupContainer errorMessagesContainer = new WebMarkupContainer("errorFeedbackUl")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(anyMessage(MessageType.ERROR));
			}
		};
		add(errorMessagesContainer);
		errorMessageListView = new MessageListView("messages", MessageType.ERROR);
		errorMessageListView.setVersioned(false);
		errorMessagesContainer.add(errorMessageListView);

		WebMarkupContainer warningMessagesContainer = new WebMarkupContainer("warningFeedbackUl")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(anyMessage(MessageType.WARNING));
			}
		};
		add(warningMessagesContainer);
		warningMessageListView = new MessageListView("messages", MessageType.WARNING);
		warningMessageListView.setVersioned(false);
		warningMessagesContainer.add(warningMessageListView);

		WebMarkupContainer infoMessagesContainer = new WebMarkupContainer("infoFeedbackUl")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(anyMessage(MessageType.INFO));
			}
		};
		add(infoMessagesContainer);
		infoMessageListView = new MessageListView("messages", MessageType.INFO);
		infoMessageListView.setVersioned(false);
		infoMessagesContainer.add(infoMessageListView);

	}
	
	public enum MessageType {INFO, WARNING, ERROR}
	
	/**
	 * Search messages that this panel will render, and see if there is any message.
	 * 
	 * @return whether there is any message for this panel
	 */
	public final boolean anyMessage(MessageType messageType)
	{
		return anyMessage(FeedbackMessage.UNDEFINED, messageType);
	}
	
	/**
	 * Search messages that this panel will render, and see if there is any message of the given
	 * level.
	 * 
	 * @param level
	 *            the level, see FeedbackMessage
	 * @return whether there is any message for this panel of the given level
	 */
	public final boolean anyMessage(int level, MessageType messageType)
	{
		List<FeedbackMessage> msgs = null;
		if( messageType.equals(MessageType.ERROR) ) {
			msgs = getCurrentErrorMessages();
			level = FeedbackMessage.ERROR;
		}
		else if( messageType.equals(MessageType.WARNING) ) {
			msgs = getCurrentWarningMessages();
			level = FeedbackMessage.WARNING;
		}
		else if( messageType.equals(MessageType.INFO) ) {
			msgs = getCurrentInfoMessages();
			level = FeedbackMessage.INFO;
		}
		
		for (FeedbackMessage msg : msgs)
		{
			if (msg.isLevel(level))
			{
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Gets the css class for the given message.
	 * 
	 * @param message
	 *            the message
	 * @return the css class; by default, this returns feedbackPanel + the message level, eg
	 *         'feedbackPanelERROR', but you can override this method to provide your own
	 */
	protected String getCSSClass(final FeedbackMessage message)
	{
		return "feedbackPanel" + message.getLevelAsString();
	}



	/**
	 * Gets the currently collected messages for this panel.
	 * 
	 * @return the currently collected messages for this panel, possibly empty
	 */
	protected final List<FeedbackMessage> getCurrentErrorMessages()
	{
		final List<FeedbackMessage> messages = errorMessageListView.getModelObject();
		return Collections.unmodifiableList(messages);
	}
	
	/**
	 * Gets the currently collected messages for this panel.
	 * 
	 * @return the currently collected messages for this panel, possibly empty
	 */
	protected final List<FeedbackMessage> getCurrentWarningMessages()
	{
		final List<FeedbackMessage> messages = warningMessageListView.getModelObject();
		return Collections.unmodifiableList(messages);
	}
	
	/**
	 * Gets the currently collected messages for this panel.
	 * 
	 * @return the currently collected messages for this panel, possibly empty
	 */
	protected final List<FeedbackMessage> getCurrentInfoMessages()
	{
		final List<FeedbackMessage> messages = infoMessageListView.getModelObject();
		return Collections.unmodifiableList(messages);
	}

	/**
	 * Gets a new instance of FeedbackMessagesModel to use.
	 * 
	 * @return Instance of FeedbackMessagesModel to use
	 */
	protected FeedbackMessagesModel newFeedbackMessagesModel()
	{
		return new FeedbackMessagesModel(this);
	}

	/**
	 * Generates a component that is used to display the message inside the feedback panel. This
	 * component must handle being attached to <code>span</code> tags.
	 * 
	 * By default a {@link Label} is used.
	 * 
	 * Note that the created component is expected to respect feedback panel's
	 * {@link #getEscapeModelStrings()} value
	 * 
	 * @param id
	 *            parent id
	 * @param message
	 *            feedback message
	 * @return component used to display the message
	 */
	protected Component newMessageDisplayComponent(String id, FeedbackMessage message)
	{
		Serializable serializable = message.getMessage();
		Label label = new Label(id, (serializable == null) ? "" : serializable.toString());
		label.setEscapeModelStrings(ExtendedFeedbackPanel.this.getEscapeModelStrings());
		return label;
	}
}
