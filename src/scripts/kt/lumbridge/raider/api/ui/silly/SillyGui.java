package scripts.kt.lumbridge.raider.api.ui.silly;

import org.tribot.script.sdk.util.Resources;

import javax.swing.*;
import java.awt.*;

public class SillyGui extends JFrame {
    private final JFrame rootFrame;

    public SillyGui(JFrame rootFrame) throws HeadlessException {
        this.rootFrame = rootFrame;

        ImageIcon icon = new ImageIcon(
                Resources.getImage("scripts/kt/lumbridge/raider/api/resources/Silly.png")
        );

        JLabel label = new JLabel(icon);
        add(label);
        pack();
        setSize(400, 500);
        setResizable(false);
        setLocationRelativeTo(rootFrame);
    }
}
