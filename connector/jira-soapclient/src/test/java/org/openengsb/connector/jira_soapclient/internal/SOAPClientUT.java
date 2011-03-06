package org.openengsb.connector.jira_soapclient.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.connector.jira_soapclient.internal.SOAPSession;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.RemoteComment;
import com.atlassian.jira.rpc.soap.client.RemoteComponent;
import com.atlassian.jira.rpc.soap.client.RemoteCustomFieldValue;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira.rpc.soap.client.RemoteVersion;


/**
 * SOAPClient is an example of the SOAP APIs offered by JIRA.
 * <p/>
 * It is designed to be run against http://jira.atlassian.com.
 * <p/>
 * NOTE : This is not a "client side API" per se.  Its an example of how to use
 * the JIRA SOAP API as a client, and some of the calls available.
 * <p/>
 * If you want to see more SOAP example code, have a look at the com.atlassian.jira_soapclient.exercise
 * and the code therein.  This code is used by our functional test framework to
 * run SOAP API calls and assert that they have the desired affect on a JIRA instance.
 * <p/>
 */
public class SOAPClientUT {
    /*
     * NOTE : JIRA DEV TEAM - do NOT try to make this class perform both the duties of a
     * SOAP sample and functional test verifier.  Its leads to unmaintainable code.  This class is purely
     * for people to see SOAP API calls in action against http://jira.atlassian.com
     *
     * The exercise package has functional test related code as do the function test cases themselves.
     *
     */

    // Login details
    static final String LOGIN_NAME = "soaptester";
    static final String LOGIN_PASSWORD = "soaptester";

    // Constants for issue creation
    static final String PROJECT_KEY = "TST";
    static final String ISSUE_TYPE_ID = "1";
    static final String SUMMARY_NAME = "An issue created via the JIRA SOAPClient sample : " + new Date();
    static final String PRIORITY_ID = "4";
    static final String COMPONENT_ID = "10240";
    static final String VERSION_ID = "10330";

    // Constants for issue update
    static final String NEW_SUMMARY = "New summary";
    static final String CUSTOM_FIELD_KEY_1 = "customfield_10061";
    static final String CUSTOM_FIELD_VALUE_1 = "10098";
    static final String CUSTOM_FIELD_KEY_2 = "customfield_10061:1";
    static final String CUSTOM_FIELD_VALUE_2 = "10105";

    // Constant for add comment
    static final String NEW_COMMENT_BODY = "This is a new comment";

    // Constant for get filter
    static final String FILTER_ID_FIXED_FOR_RELEASED_VERSION = "12355"; /// Fixed for released versions
    static final String SOAP_AS_A_SEARCH_TERM = "SOAPClient";
    private static JiraSoapService jiraSoapService;
    private static String authToken;
    private static RemoteIssue issue = new RemoteIssue();


    @BeforeClass
    public static void setUpClass() throws Exception {
        String baseUrl = "http://jira.atlassian.com/rpc/soap/jirasoapservice-v2";
        Timing timing = Timing.startTiming("JIRA SOAP client sample");
        try {
            // get  handle to the JIRA SOAP Service from a client point of view
            SOAPSession soapSession = new SOAPSession(new URL(baseUrl));

            // connect to JIRA
            Timing loginTiming = Timing.startTiming("Login");
            try {
                soapSession.connect(LOGIN_NAME, LOGIN_PASSWORD);
            } finally {
                loginTiming.printTiming();
            }

            // the JIRA SOAP Service and authentication token are used to make authentication calls
            jiraSoapService = soapSession.getJiraSoapService();
            authToken = soapSession.getAuthenticationToken();

            issue = testCreateIssue(jiraSoapService, authToken);


        } finally {
            timing.printTiming();
        }
    }

    @Test
    public void testGetIssueById() throws RemoteException {
        Timing timing = Timing.startTiming("GetIssueById");
        try {

            System.out.println("Testing getIssueById ...");
            RemoteIssue newIssue = jiraSoapService.getIssueById(authToken, issue.getId());
            System.out.println("\tReturned an issue id: " + newIssue.getId() + " key: " + newIssue.getKey());

        } finally {
            timing.printTiming();
        }
    }

    @Test
    public void testGetVersions() throws RemoteException {
        Timing timing = Timing.startTiming("GetVersions");
        try {

            System.out.println("Testing getIssueById ...");
            RemoteVersion[] newIssue = jiraSoapService.getVersions(authToken, PROJECT_KEY);
            System.out.println(newIssue.length);
        } finally {
            timing.printTiming();
        }
    }


    @Test
    public void testAddAttachment() throws IOException {
        Timing timing = Timing.startTiming("AddAttachment");
        try {

            File tmpFile = File.createTempFile("attachment", ".txt");
            FileWriter fw = new FileWriter(tmpFile);
            fw.write("A sample file attached via SOAP to JIRA issue " + issue.getKey());
            fw.close();

            boolean added = jiraSoapService
                .addAttachmentsToIssue(authToken, issue.getKey(), new String[]{tmpFile.getName()},
                    new byte[][]{getBytesFromFile(tmpFile)});
            System.out.println(
                "\t" + (added ? "Added" : "Failed to add") + " attachment " + tmpFile.getName() + " to issue " + issue
                    .getKey());
            tmpFile.delete();

        } finally {
            timing.printTiming();
        }

    }


    @Test
    public void testAddComment() throws java.rmi.RemoteException {
        Timing timing = Timing.startTiming("AddComment");
        try {

            // Adding a comment
            final RemoteComment comment = new RemoteComment();
            comment.setBody(NEW_COMMENT_BODY);
            jiraSoapService.addComment(authToken, issue.getKey(), comment);

        } finally {
            timing.printTiming();
        }
    }

    private static RemoteIssue testCreateIssue(JiraSoapService jiraSoapService, String token)
        throws java.rmi.RemoteException {
        Timing timing = Timing.startTiming("CreateIssue");
        try {
            // Create the issue
            RemoteIssue issue = new RemoteIssue();
            issue.setProject(PROJECT_KEY);
            issue.setType(ISSUE_TYPE_ID);

            issue.setSummary(SUMMARY_NAME);
            issue.setPriority(PRIORITY_ID);
            issue.setDuedate(Calendar.getInstance());
            issue.setAssignee("");

            // Add remote compoments
            RemoteComponent component = new RemoteComponent();
            component.setId(COMPONENT_ID);
            issue.setComponents(new RemoteComponent[]{component});

            // Add remote versions
            RemoteVersion version = new RemoteVersion();
            version.setId(VERSION_ID);
            RemoteVersion[] remoteVersions = new RemoteVersion[]{version};
            issue.setFixVersions(remoteVersions);

            // Add custom fields
            RemoteCustomFieldValue customFieldValue = new RemoteCustomFieldValue(CUSTOM_FIELD_KEY_1, "",
                new String[]{CUSTOM_FIELD_VALUE_1});
            RemoteCustomFieldValue customFieldValue2 = new RemoteCustomFieldValue(CUSTOM_FIELD_KEY_2, "",
                new String[]{CUSTOM_FIELD_VALUE_2});
            RemoteCustomFieldValue[] customFieldValues = new RemoteCustomFieldValue[]{customFieldValue,
                customFieldValue2};
            issue.setCustomFieldValues(customFieldValues);

            // Run the create issue code
            RemoteIssue returnedIssue = jiraSoapService.createIssue(token, issue);
            final String issueKey = returnedIssue.getKey();

            System.out.println("\tSuccessfully created issue " + issueKey);
            printIssueDetails(returnedIssue);
            return returnedIssue;
        } finally {
            timing.printTiming();
        }


    }

    private static void printIssueDetails(RemoteIssue issue) {
        System.out.println("Issue Details : ");
        Method[] declaredMethods = issue.getClass().getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            Method declaredMethod = declaredMethods[i];
            if (declaredMethod.getName().startsWith("get") && declaredMethod.getParameterTypes().length == 0) {
                System.out.print("\t Issue." + declaredMethod.getName() + "() -> ");
                try {
                    Object obj = declaredMethod.invoke(issue, new Object[]{});
                    if (obj instanceof Object[]) {
                        obj = arrayToStr((Object[]) obj);
                    } else {
                    }
                    System.out.println(obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String arrayToStr(Object[] o) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < o.length; i++) {
            sb.append(o[i]).append(" ");
        }
        return sb.toString();
    }

    // Returns the contents of the file in a byte array.
    // From http://javaalmanac.com/egs/java.io/File2ByteArray.html

    private byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length < Integer.MAX_VALUE) {
            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }

            // Close the input stream and return bytes
            is.close();
            return bytes;
        } else {
            System.out.println("File is too large");
            return null;
        }
    }


    private static class Timing {
        private String operationDesc;
        private long then;

        private Timing(final String operationDesc) {
            this.operationDesc = operationDesc;
            this.then = System.currentTimeMillis();
        }

        private static Timing startTiming(String operationDesc) {
            System.out.println("\nRunning : " + operationDesc);
            return new Timing(operationDesc);
        }

        private void printTiming() {
            final long howLong = System.currentTimeMillis() - this.then;
            System.out.println("________________________________________________________________");
            DecimalFormat decFormat = new DecimalFormat("###,##0");
            System.out.println("\t" + this.operationDesc + " took " + decFormat.format(howLong) + " ms to run");
        }
    }
}
