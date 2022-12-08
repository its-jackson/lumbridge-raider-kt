/*
 * Created by JFormDesigner on Thu Dec 08 14:18:07 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.task.questing;

import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptQuestingData;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.behaviors.questing.Quest;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Polymorphic
 */
public class QuestingGuiTask extends JFrame {
    private final ScriptTaskGui rootFrame;

    public QuestingGuiTask(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();
        comboBox1.addItem(Quest.COOKS_ASSISTANT);
    }

    public void showQuestingAddForm() {
        setVisible(true);
    }

    private void ok(ActionEvent e) {
        if (comboBox1.getSelectedItem() == null) return;

        ScriptTask questTask = new ScriptTask.Builder()
                .behavior(ScriptBehavior.QUESTING)
                .questingData(new ScriptQuestingData((Quest) comboBox1.getSelectedItem()))
                .build();

        rootFrame.getScriptTaskDefaultListModel().addElement(questTask);
        setVisible(false);
    }

    private void cancel(ActionEvent e) {
        setVisible(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        comboBox1 = new JComboBox();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle("Quest Task");
        setMinimumSize(new Dimension(300, 200));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setBorder(new TitledBorder(""));
                contentPanel.setLayout(new GridBagLayout());

                //---- label1 ----
                label1.setText("Pick Quest");
                contentPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
                contentPanel.add(comboBox1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setText("OK");
                okButton.addActionListener(e -> ok(e));
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.addActionListener(e -> cancel(e));
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JComboBox comboBox1;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
