/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.ui.web;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;

//import org.mortbay.jetty.Connector;
//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.bio.SocketConnector;
//import org.mortbay.jetty.webapp.WebAppContext;

public class StartDevelopmentServer {

//    public static void main(String[] args) throws Exception {
//        new StartDevelopmentServer().start();
//    }
//
//    private static final String CONTEXT_PATH = "/";
//    private static final String FINAL_WAR_PATH = "target/webapp";
//    private static final String WEB_XML_PATH_TEST = "src/test/webapp/WEB-INF/web.xml";
//    private static final String WEB_XML_PATH_ORIGIN = FINAL_WAR_PATH + "/WEB-INF/web.xml";
//    private static final String ORIGIN_WAR_PATH = "src/main/webapp";
//    private static final String ORIGIN_DOCS_FOLDER = "src/test/webapp/docs/";
//    private static final String FINAL_DOCS_FOLDER = "target/webapp/docs/";
//
//    private Server server = new Server();
//    private SocketConnector connector = new SocketConnector();
//    WebAppContext context = new WebAppContext();
//
//    public StartDevelopmentServer() throws IOException {
//        configureConnector();
//        configureWebappContext();
//        startJmxServer();
//        configureServer();
//    }
//
//    private void configureConnector() {
//        // Set some timeout options to make debugging easier.
//        connector.setMaxIdleTime(1000 * 60 * 60);
//        connector.setSoLingerTime(-1);
//        connector.setPort(8080);
//    }
//
//    private void configureWebappContext() throws IOException {
//        context.setServer(server);
//        context.setContextPath(CONTEXT_PATH);
//        prepareFileSystem();
//        context.setWar(FINAL_WAR_PATH);
//    }
//
//    private void prepareFileSystem() throws IOException {
//        deleteCurrentWorkingDir();
//        copyBaseFiles();
//        deleteOriginWebXml();
//        copyTestWebXml();
//        copyDocsFolder();
//    }
//
//    private void deleteCurrentWorkingDir() {
//        File workingDir = new File(FINAL_WAR_PATH);
//        if (workingDir.exists()) {
//            deleteDir(workingDir);
//        }
//    }
//
//    private void copyBaseFiles() throws IOException {
//        File source = new File(ORIGIN_WAR_PATH);
//        File target = new File(FINAL_WAR_PATH);
//        copyDirectory(source, target);
//    }
//
//    public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
//        if (sourceLocation.isDirectory()) {
//            if (!targetLocation.exists()) {
//                targetLocation.mkdir();
//            }
//            String[] children = sourceLocation.list();
//            for (int i = 0; i < children.length; i++) {
//                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
//            }
//        } else {
//            InputStream in = new FileInputStream(sourceLocation);
//            OutputStream out = new FileOutputStream(targetLocation);
//            copyInputStreamToOutputStream(in, out);
//        }
//    }
//
//    private void deleteOriginWebXml() {
//        new File(WEB_XML_PATH_ORIGIN).delete();
//    }
//
//    private void copyTestWebXml() throws IOException {
//        File input = new File(WEB_XML_PATH_TEST);
//        File target = new File(WEB_XML_PATH_ORIGIN);
//        copyInputStreamToOutputStream(new FileInputStream(input), new FileOutputStream(target));
//    }
//
//    private void copyDocsFolder() throws IOException {
//        new File(FINAL_DOCS_FOLDER).mkdirs();
//        File input = new File(ORIGIN_DOCS_FOLDER);
//        File output = new File(FINAL_DOCS_FOLDER);
//        copyDirectory(input, output);
//    }
//
//    private void copyInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException {
//        byte[] buf = new byte[1024];
//        int len;
//        while ((len = in.read(buf)) > 0) {
//            out.write(buf, 0, len);
//        }
//        in.close();
//        out.close();
//    }
//
//    private boolean deleteDir(File dir) {
//        if (dir.isDirectory()) {
//            String[] children = dir.list();
//            for (int i = 0; i < children.length; i++) {
//                boolean success = deleteDir(new File(dir, children[i]));
//                if (!success) {
//                    return false;
//                }
//            }
//        }
//        return dir.delete();
//    }
//
//    private void startJmxServer() {
//        // START JMX SERVER
//        // MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
//        // MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
//        // server.getContainer().addEventListener(mBeanContainer);
//        // mBeanContainer.start();
//    }
//
//    private void configureServer() {
//        server.setConnectors(new Connector[] { connector });
//        server.addHandler(context);
//    }
//
//    public void start() {
//        try {
//            System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
//            server.start();
//            System.in.read();
//            System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
//            server.stop();
//            server.join();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(100);
//        }
//    }

}
