package com.solitaire;

import javax.swing.SwingUtilities;


public class TestGUI {

    /**
     * Main method to launch the GUI application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI frame = new GUI();
            frame.setVisible(true);
        });
    }
}