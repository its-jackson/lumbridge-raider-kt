/*
 * Created by JFormDesigner on Tue Dec 06 21:40:52 AST 2022
 */

package scripts.kt.lumbridge.raider.api.ui.task.cooking;

import java.awt.event.*;

import scripts.kotlin.api.ResourceGainedCondition;
import scripts.kt.lumbridge.raider.api.ScriptBehavior;
import scripts.kt.lumbridge.raider.api.ScriptTask;
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Polymorphic
 */
public class CookingTaskGui extends JFrame {
    private final ScriptTaskGui rootFrame;

    private int editIndex;

    public CookingTaskGui(ScriptTaskGui rootFrame) {
        this.rootFrame = rootFrame;
        initComponents();
    }

    public void showCookingAddForm() {
        okButton.setText("Add");
        editIndex = -1;
        textField1.setText("");
        textField2.setText("");
        setVisible(true);
    }

    public void showCookingEditForm(ScriptTask selectedTask, int selectedIndex) {
        if (selectedTask == null || selectedIndex == -1) return;
        if (selectedTask.getResourceGainedCondition() == null) return;

        okButton.setText("Save");
        editIndex = selectedIndex;
        textField1.setText(String.valueOf(selectedTask.getResourceGainedCondition().getId()));
        textField2.setText(String.valueOf(selectedTask.getResourceGainedCondition().getAmount()));
        setVisible(true);
    }

    private void ok(ActionEvent e) {
        String foodIdStr = textField1.getText()
                .trim();

        String amountToCookStr = textField2.getText()
                .trim();

        int foodId;
        int amountToCook = -1;

        try {
            foodId = Integer.parseInt(foodIdStr);
        }
        catch (NumberFormatException formatException) {
            return;
        }
        try {
            amountToCook = Integer.parseInt(amountToCookStr);
        }
        catch (NumberFormatException ignored) {
            if (!amountToCookStr.isEmpty()) return;
        }

        ScriptTask cookingTask = new ScriptTask.Builder()
                .behavior(ScriptBehavior.COOKING)
                .stopCondition(new ResourceGainedCondition(foodId, amountToCook))
                .build();

        if (editIndex != -1)
            rootFrame.getScriptTaskDefaultListModel().set(editIndex, cookingTask);
        else
            rootFrame.getScriptTaskDefaultListModel().addElement(cookingTask);

        setVisible(false);
    }

    private void cancel(ActionEvent e) {
        setVisible(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        label1 = new JLabel();
        textField1 = new JTextField();
        label2 = new JLabel();
        textField2 = new JTextField();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle("Cooking Task");
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new BorderLayout());

                //======== panel1 ========
                {
                    panel1.setBorder(new BevelBorder(BevelBorder.LOWERED));
                    panel1.setLayout(new GridBagLayout());

                    //---- label1 ----
                    label1.setText("Food ID");
                    panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                    panel1.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //---- label2 ----
                    label2.setText("Amount To Cook (leave blank, or -1, for until fully depleted.)");
                    panel1.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                    panel1.add(textField2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));
                }
                contentPanel.add(panel1, BorderLayout.CENTER);
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
    private JPanel panel1;
    private JLabel label1;
    private JTextField textField1;
    private JLabel label2;
    private JTextField textField2;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
