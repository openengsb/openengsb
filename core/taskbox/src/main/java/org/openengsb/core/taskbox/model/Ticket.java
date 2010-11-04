package org.openengsb.core.taskbox.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Ticket implements Task {
	private String id;
	private String type;
	
	
	
	//stores detailed history info about the lifecycle of the ticket
	private List<String> history;
	
	//stores general info about the Ticket
	//e.g. mailtext if the ticket was created out of a mail
	private List<String> notes;
	
	public Ticket(String id) {
		super();
		this.id = id;
		this.history = new ArrayList<String>();
		this.notes = new ArrayList<String>();
		
		this.addHistoryEntry("created empty Ticket");
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		String oldId = this.id;
		this.id = id;
		
		this.addHistoryEntry("changed Id from <" + oldId + "> to <" + id + ">");
	}
	
	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		String oldType = this.type;
		this.type = type;
		
		this.addHistoryEntry("changed Type from <" + oldType + "> to <" + type + ">");
	}
	
	private void addHistoryEntry(String historyEntry) {
		//functionality could also be used to store 'Events' for BusinessCalculations
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String timestamp = dateFormat.format(date);
		
		this.history.add(timestamp + " - " + historyEntry);
	}
	
	public void addNoteEntry(String noteEntry) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String timestamp = dateFormat.format(date);
		
		this.notes.add("--------------------- " + timestamp + " ---------------------\n" + 
			noteEntry);
		
		this.addHistoryEntry("added new NoteEntry");
	}
}
