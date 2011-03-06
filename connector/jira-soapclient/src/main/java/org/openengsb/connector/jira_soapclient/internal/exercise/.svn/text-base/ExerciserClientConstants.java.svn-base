package com.atlassian.jira_soapclient.exercise;

import java.io.FileInputStream;
import java.util.Date;

/**
 * Large pile of constants used when exercising code
 */
public class ExerciserClientConstants
{


    // Login details
    public static String LOGIN_NAME = "admin";
    public static String LOGIN_PASSWORD = "admin";
    public static String LOGIN_NAME_GROUP = "jira-administrators";

    // Constants for issue creation
    public static String PROJECT_KEY = "HSP";
    public static String ISSUE_TYPE_ID = "1";
    public static String SUMMARY_NAME = "This is a new SOAP issue " + new Date();
    public static String PRIORITY_ID = "4";
    public static String COMPONENT_ID = "10000";
    public static String VERSION_ID = "10000";

    // Constants for issue update
    public static String NEW_SUMMARY = "New summary";
    public static String CUSTOM_FIELD_KEY_1 = "customfield_10000";
    public static String CUSTOM_FIELD_VALUE_1 = "SOAP CUSTOM FIELD VALUE 1";
    public static String CUSTOM_FIELD_KEY_2 = "customfield_10001";
    public static String CUSTOM_FIELD_VALUE_2 = "SOAP CUSTOM FIELD VALUE 2";
    public static String CUSTOM_FIELD_KEY_3 = "customfield_10010";
    public static String CUSTOM_FIELD_VALUE_3 = "10000";
    public static String CUSTOM_FIELD_VALUE_4 = "10002";


    // Constants for project creation
    public static String CREATE_PROJECT_KEY = "DUD";
    public static String PROJECT_NAME = "Dee Project";
    public static String PROJECT_DESCRIPTION = "This is a project created by soap on: " + new Date();

    // Constants for Project Roles
    public static String USER_PROJECT_ROLE_ID = "10000";
    public static String USER_PROJECT_ROLE_NAME = "Users";
    public static String DEVELOPER_PROJECT_ROLE_ID = "10001";
    public static String DEVELOPER_PROJECT_ROLE_NAME = "Developers";
    public static String MONKEY_PROJECT_ID = "10001";
    public static String MONKEY_PROJECT_NAME = "monkey";
    public static String MONKEY_PROJECT_KEY = "MKY";

    public static String FILTER_ID = "10000";

    private static String PROPERTIES_FILE = "ClientConstants.properties";

    static
    {
        try

        {
            // read the properties file if present
            java.util.Properties props = new java.util.Properties();
            java.io.InputStream propsStream = null;

            String propertiesFileName = System.getProperty("test.soapclient.constants.properties", PROPERTIES_FILE);

            try
            {
                // See if it is in the classpath
                propsStream = ExerciserClientConstants.class.getResourceAsStream("/WEB-INF/" + propertiesFileName);
                // See if it is in the jar
                if (null == propsStream)
                {
                    propsStream = ExerciserClientConstants.class.getResourceAsStream("/" + propertiesFileName);
                    if (null == propsStream)
                    {
                        propsStream = ExerciserClientConstants.class.getResourceAsStream(propertiesFileName);
                        if (null == propsStream)
                        {
                            // The resource was not found on the classpath. Try opening as a file
                            propsStream = new FileInputStream(propertiesFileName);
                        }

                    }
                }
                props.load(propsStream);
            }
            catch (Throwable t)
            {
                System.err.println("## ExerciserClientConstants unable to find the properties file ClientConstants.properties");
            }

            // update field values as appropriate
            java.lang.reflect.Field[] fields = ExerciserClientConstants.class.getFields();
            for (int i = 0; i < fields.length; i++)
            {
                String name = fields[i].getName();
                String value = props.getProperty(name);
                if (value != null)
                {
                    value = value.trim();
                    String oVal = (String) value;
                    fields[i].set(null, oVal);
                }
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.err.println("## ExerciserClientConstants init error: " + t);
        }
    }

}
