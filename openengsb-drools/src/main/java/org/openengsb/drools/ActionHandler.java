package org.openengsb.drools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.servicemix.drools.model.JbiHelper;
import org.openengsb.drools.model.Action;

public class ActionHandler {

	JbiHelper jbihelper;
	Class<? extends JbiHelper> jbihelperclass = JbiHelper.class;

	public ActionHandler(JbiHelper jbihelper) {
		this.jbihelper = jbihelper;
	}

	public boolean handleAction(Action action) {
		String name = action.getName();
		Object[] args = action.getArgs().toArray();
		System.out.println("triggering action " + name);
		Class<?>[] types = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			types[i] = args[i].getClass();
		}
		Method m;
		try {
			m = JbiHelper.class.getMethod(name, types);
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		}
		try {
			m.invoke(jbihelper, args);
			System.out.println("action triggered");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
