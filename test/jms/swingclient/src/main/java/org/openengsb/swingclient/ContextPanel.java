package org.openengsb.swingclient;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class ContextPanel extends JPanel {

    private JButton refresh = new JButton("Refresh");
    private JButton exit = new JButton("Exit");

    ContextTreePanel tree = new ContextTreePanel(this);

    ContextTablePanel table = new ContextTablePanel(this);

    public ContextPanel() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refresh);
        buttonPanel.add(exit);

        refresh.addActionListener(new RefreshContextAction(this));
        exit.addActionListener(new ExitAction());

        JSplitPane centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerPanel.setTopComponent(new JScrollPane(tree));
        centerPanel.setBottomComponent(table);

        add(centerPanel);
        add(buttonPanel, BorderLayout.SOUTH);

    }

}
