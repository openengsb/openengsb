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
package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;

public class LoadAction implements ActionListener {

    private MessagePanel panel;

    public LoadAction(MessagePanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(panel);
        File file = fileChooser.getSelectedFile();
        if (file == null) {
            return;
        }

        try {
            panel.textArea.setText(IOUtils.toString(new FileReader(file)));
            panel.textArea.setCaretPosition(0);
            panel.textArea.requestFocus();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(panel, "File could not be found", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(panel, "Error reading file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
