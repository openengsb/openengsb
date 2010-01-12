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
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class DeleteContextEntryAction implements ActionListener {

    private ContextTablePanel panel;
    private ContextFacade contextFacade = new ContextFacade();

    public DeleteContextEntryAction(ContextTablePanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        List<ContextEntry> model = panel.getModel();
        int row = panel.table.getSelectedRow();

        int result = JOptionPane.showConfirmDialog(panel, "Do you really want to delete this entry?", "Delete Entry",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        ContextEntry removedEntry = model.remove(row);

        ((AbstractTableModel) panel.table.getModel()).fireTableDataChanged();
        contextFacade.remove(removedEntry.getPath() + removedEntry.getName());
    }
}
