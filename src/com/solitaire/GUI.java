package com.solitaire;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

public class GUI extends JFrame {

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 500;
    private static final Color HEADER_COLOR = new Color(41, 128, 185);
    private static final Color PANEL_COLOR = new Color(236, 240, 241);
    private static final Color ACCENT_COLOR = new Color(231, 76, 60);

    private JCheckBox recordGameCheckbox;
    private JRadioButton englishRadio;
    private JRadioButton hexagonRadio;
    private JRadioButton diamondRadio;

    /**
     * Constructs the main GUI window with all components.
     */
    public GUI() {
        setTitle("Solitaire Game - GUI Example");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    /**
     * Creates the header panel with colored background and title text.
     *
     * @return JPanel containing the header
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 80));

        JLabel titleLabel = new JLabel("Peg Solitaire BrainVita Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        return headerPanel;
    }

    /**
     * Creates the main content panel with board type options and checkbox.
     *
     * @return JPanel containing the main content
     */
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(15, 15));
        contentPanel.setBackground(PANEL_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentPanel.add(new LineDrawingPanel(), BorderLayout.NORTH);
        contentPanel.add(createBoardTypePanel(), BorderLayout.CENTER);
        contentPanel.add(createCheckboxPanel(), BorderLayout.SOUTH);

        return contentPanel;
    }

    /**
     * Creates the board type selection panel with radio buttons.
     *
     * @return JPanel containing radio buttons for board type selection
     */
    private JPanel createBoardTypePanel() {
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(4, 1, 5, 10));
        radioPanel.setBackground(Color.WHITE);
        radioPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(HEADER_COLOR, 2),
                "Board Type",
                0,
                0,
                new Font("Arial", Font.BOLD, 16),
                HEADER_COLOR));

        englishRadio = new JRadioButton("English Board");
        hexagonRadio = new JRadioButton("Hexagon Board");
        diamondRadio = new JRadioButton("Diamond Board");

        englishRadio.setFont(new Font("Arial", Font.PLAIN, 14));
        hexagonRadio.setFont(new Font("Arial", Font.PLAIN, 14));
        diamondRadio.setFont(new Font("Arial", Font.PLAIN, 14));

        englishRadio.setBackground(Color.WHITE);
        hexagonRadio.setBackground(Color.WHITE);
        diamondRadio.setBackground(Color.WHITE);

        englishRadio.setSelected(true);

        ButtonGroup boardGroup = new ButtonGroup();
        boardGroup.add(englishRadio);
        boardGroup.add(hexagonRadio);
        boardGroup.add(diamondRadio);

        radioPanel.add(new JLabel("  Select your preferred board:"));
        radioPanel.add(englishRadio);
        radioPanel.add(hexagonRadio);
        radioPanel.add(diamondRadio);

        return radioPanel;
    }

    /**
     * Creates the checkbox panel for game recording option.
     *
     * @return JPanel containing the checkbox
     */
    private JPanel createCheckboxPanel() {
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.setBackground(PANEL_COLOR);

        recordGameCheckbox = new JCheckBox("Record game for replay");
        recordGameCheckbox.setFont(new Font("Arial", Font.BOLD, 14));
        recordGameCheckbox.setForeground(ACCENT_COLOR);
        recordGameCheckbox.setBackground(PANEL_COLOR);

        checkboxPanel.add(recordGameCheckbox);

        return checkboxPanel;
    }

    /**
     * Creates the button panel at the bottom of the window.
     *
     * @return JPanel containing action buttons
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(189, 195, 199));

        JButton newGameButton = new JButton("New Game");
        JButton exitButton = new JButton("Exit");

        newGameButton.setBackground(new Color(46, 204, 113));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        newGameButton.setFocusPainted(false);

        exitButton.setBackground(ACCENT_COLOR);
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setFocusPainted(false);

        exitButton.addActionListener(e -> System.exit(0));
        newGameButton.addActionListener(e ->
                System.out.println("New game started with board type: " + getSelectedBoardType()));

        buttonPanel.add(newGameButton);
        buttonPanel.add(exitButton);

        return buttonPanel;
    }

    /**
     * Gets the currently selected board type.
     *
     * @return String representation of the selected board type
     */
    public String getSelectedBoardType() {
        if (englishRadio.isSelected()) {
            return "English";
        } else if (hexagonRadio.isSelected()) {
            return "Hexagon";
        } else if (diamondRadio.isSelected()) {
            return "Diamond";
        }
        return "None";
    }

    /**
     * Checks if game recording is enabled.
     *
     * @return true if record game checkbox is selected, false otherwise
     */
    public boolean isRecordingEnabled() {
        return recordGameCheckbox.isSelected();
    }

    /**
     * Custom panel that draws colored lines.
     */
    private class LineDrawingPanel extends JPanel {

        private static final int PANEL_HEIGHT = 60;

        LineDrawingPanel() {
            setPreferredSize(new Dimension(WINDOW_WIDTH, PANEL_HEIGHT));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(new Color(52, 152, 219));
            g.drawLine(50, 20, 550, 20);

            g.setColor(new Color(46, 204, 113));
            g.drawLine(50, 30, 550, 30);

            g.setColor(new Color(155, 89, 182));
            g.drawLine(50, 40, 550, 40);

            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 12));
            g.drawString("Sprint 0: GUI Demonstration", 180, 15);
        }
    }
}