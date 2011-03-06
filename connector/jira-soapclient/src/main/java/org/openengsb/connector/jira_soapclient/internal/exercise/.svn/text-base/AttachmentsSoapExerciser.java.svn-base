package com.atlassian.jira_soapclient.exercise;

import com.atlassian.jira.rpc.soap.client.RemoteAttachment;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import com.atlassian.jira_soapclient.SOAPSession;
import org.apache.axis.encoding.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class exercises the <b>Attachments</b> functions of the JIRA SOAP API
 */
public class AttachmentsSoapExerciser extends AbstractSoapExerciser
{
    public AttachmentsSoapExerciser(final SOAPSession soapSession)
    {
        super(soapSession);
    }

    public String testAddAttachment(RemoteIssue issue) throws IOException
    {
        AttachmentBean ab = createAttachment("A sample file attached via SOAP to JIRA issue " + issue.getKey());

        boolean added = getJiraSoapService().addAttachmentsToIssue(getToken(),
                issue.getKey(),
                AttachmentResolver.getFileNames(ab),
                AttachmentResolver.getByteArrayData(ab));
        System.out.println((added ? "Added" : "Failed to add") + " attachment " + ab.getFileName() + " to issue " + issue.getKey());
        return ab.getFileName();
    }

    public String[] testAddMultipleAttachmentsUsingBase64(RemoteIssue issue) throws IOException
    {
        List attachments = createMultipleAttachments(issue.getKey(), 5);
        String[] fileNames = AttachmentResolver.getFileNames(attachments);

        boolean added = getJiraSoapService().addBase64EncodedAttachmentsToIssue(getToken(),
                issue.getKey(),
                fileNames,
                AttachmentResolver.getBase64Data(attachments));
        System.out.println((added ? "Added" : "Failed to add") + " attachments " + fileNames.toString() + " to issue " + issue.getKey());
        return fileNames;
    }

    public String testAddLargeAttachmentUsingBase64(RemoteIssue issue, int fileSize) throws IOException
    {
        AttachmentBean ab = createAttachment("A sample file attached via SOAP to JIRA issue " + issue.getKey(), fileSize);

        boolean added = getJiraSoapService().addBase64EncodedAttachmentsToIssue(getToken(),
                issue.getKey(),
                AttachmentResolver.getFileNames(ab),
                AttachmentResolver.getBase64Data(ab));
        System.out.println((added ? "Added" : "Failed to add") + " attachment " + ab.getFileName() + " to issue " + issue.getKey());
        return ab.getFileName();
    }

    public RemoteAttachment[] testGetAttachments(String issueKey) throws RemoteException
    {
        return getJiraSoapService().getAttachmentsFromIssue(getToken(), issueKey);
    }

    // Returns the contents of the file in a byte array.
    // From http://javaalmanac.com/egs/java.io/File2ByteArray.html
    private byte[] getBytesFromFile(File file) throws IOException
    {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length < Integer.MAX_VALUE)
        {
            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                   && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
            {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length)
            {
                throw new IOException("Could not completely read file " + file.getName());
            }

            // Close the input stream and return bytes
            is.close();
            return bytes;
        }
        else
        {
            System.out.println("File is too large");
            return null;
        }

    }

    private List/*<AttachmentBean>*/ createMultipleAttachments(String issueKey, int size) throws IOException
    {
        final String content = "A sample file attached via SOAP to JIRA issue " + issueKey;
        List attachments = new ArrayList(size);
        for (int i = 0; i < size; i++)
        {
            attachments.add(createAttachment(content));
        }
        return attachments;
    }

    private AttachmentBean createAttachment(String content) throws IOException
    {
        return createAttachment(content, content.length());
    }

    private AttachmentBean createAttachment(String content, int fileSize) throws IOException
    {
        File tmpFile = File.createTempFile("attachment", ".txt");
        FileWriter fw = new FileWriter(tmpFile);
        fw.write(getLongString(content, fileSize));
        fw.close();

        AttachmentBean ab = new AttachmentBean();
        ab.setFileName(tmpFile.getName());
        ab.setData(getBytesFromFile(tmpFile));
        tmpFile.delete();
        return ab;
    }

    /**
     * Repeats a pattern until it is of desired length.
     *
     * @param pattern the string to repeat
     * @param length the desired length
     * @return the new string
     */
    private String getLongString(String pattern, int length)
    {
        StringBuffer sb = new StringBuffer(length);
        while (sb.length() < length)
        {
            final int remainingLength = length - sb.length();
            sb.append(pattern.substring(0, Math.min(remainingLength, pattern.length())));
        }
        return sb.toString();
    }

    /**
     * Converts {@link AttachmentBean} objects into the arrays required for making the service calls.
     */
    private static class AttachmentResolver
    {
        private static String[] getFileNames(AttachmentBean attachment)
        {
            List attachments = new ArrayList();
            attachments.add(attachment);
            return getFileNames(attachments);
        }

        private static String[] getFileNames(List/*<AttachmentBean>*/ attachments)
        {
            String[] fileNames = new String[attachments.size()];
            int i = 0;
            for (Iterator iterator = attachments.iterator(); iterator.hasNext();)
            {
                AttachmentBean ab = (AttachmentBean) iterator.next();
                fileNames[i++] = ab.getFileName();
            }
            return fileNames;
        }

        private static byte[][] getByteArrayData(AttachmentBean attachment)
        {
            List attachments = new ArrayList();
            attachments.add(attachment);
            return getByteArrayData(attachments);
        }

        private static byte[][] getByteArrayData(List/*<AttachmentBean>*/ attachments)
        {
            byte[][] dataArrays = new byte[attachments.size()][];
            int i = 0;
            for (Iterator iterator = attachments.iterator(); iterator.hasNext();)
            {
                AttachmentBean ab = (AttachmentBean) iterator.next();
                byte[] data = (byte[]) ab.getData();
                dataArrays[i++] = data;
            }
            return dataArrays;
        }

        private static String[] getBase64Data(AttachmentBean attachment)
        {
            List attachments = new ArrayList();
            attachments.add(attachment);
            return getBase64Data(attachments);
        }

        private static String[] getBase64Data(List/*<AttachmentBean>*/ attachments)
        {
            String[] dataStrings = new String[attachments.size()];
            int i = 0;
            for (Iterator iterator = attachments.iterator(); iterator.hasNext();)
            {
                AttachmentBean ab = (AttachmentBean) iterator.next();
                byte[] data = (byte[]) ab.getData();
                dataStrings[i++] = Base64.encode(data);
            }
            return dataStrings;
        }
    }

    private static class AttachmentBean
    {
        private String fileName;
        private Object data;

        public String getFileName()
        {
            return fileName;
        }

        public void setFileName(final String fileName)
        {
            this.fileName = fileName;
        }

        public Object getData()
        {
            return data;
        }

        public void setData(final Object data)
        {
            this.data = data;
        }
    }

}