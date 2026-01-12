package presentation;

import javax.swing.SwingUtilities;
import presentation.ui.FrameLogin;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrameLogin frame = new FrameLogin();
            frame.setVisible(true);
        });
    }
}
