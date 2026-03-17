//CS449 Xander McKie 16329020

package com.solitaire;

import javax.swing.SwingUtilities;

/**
 * Application entry point.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI frame = new GUI();
            frame.setVisible(true);
        });
    }
}