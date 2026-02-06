import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI {

    private JFrame frame;
    private JPanel mainPanel, controlPanel, boardPanel, shapeSizePanel, bottomPanel;
    private JTextField customRows, customCols;
    private JLabel label1, label2, recordLabel, movesLabel;
    private JButton button1, button2, button3, button4, customSizeButton;
    private JToggleButton[][] boardButtons;
    private JComboBox<String> shapeComboBox, sizeComboBox;
    private int width, height;
    private int boardRows = 7, boardCols = 7;
    private String currentShape = "English";

    public GUI(int w, int h) {
        frame = new JFrame();
        width = w;
        height = h;

        // Initialize panels
        mainPanel = new JPanel(new BorderLayout());
        shapeSizePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        boardPanel = new JPanel();
        controlPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        bottomPanel = new JPanel(new BorderLayout());

        // Initialize components
        label1 = new JLabel("Board Type:");
        label2 = new JLabel("Board Size:");

        // Shape selector
        shapeComboBox = new JComboBox<>(new String[]{"English", "Hexagonal", "Diamond"});

        // Size selector
        sizeComboBox = new JComboBox<>(new String[]{"Small", "Medium", "Large", "Custom"});

        // Custom size inputs
        customRows = new JTextField("7", 3);
        customCols = new JTextField("7", 3);
        customSizeButton = new JButton("Apply");

        // Game control buttons
        button1 = new JButton("Replay");
        button2 = new JButton("New Game");
        button3 = new JButton("Autoplay");
        button4 = new JButton("Randomize");

        // Labels for moves and record
        movesLabel = new JLabel("Moves: 0");
        recordLabel = new JLabel("Record: --");

        // Initialize board buttons
        boardButtons = new JToggleButton[boardRows][boardCols];
    }

    public void setUpGUI() {
        frame.setSize(width, height);
        frame.setTitle("Peg Solitaire");

        // Top panel for shape and size selection
        shapeSizePanel.add(label1);
        shapeSizePanel.add(shapeComboBox);
        shapeSizePanel.add(Box.createHorizontalStrut(20)); // Spacer
        shapeSizePanel.add(label2);
        shapeSizePanel.add(sizeComboBox);
        shapeSizePanel.add(new JLabel("Rows:"));
        shapeSizePanel.add(customRows);
        shapeSizePanel.add(new JLabel("Cols:"));
        shapeSizePanel.add(customCols);
        shapeSizePanel.add(customSizeButton);

        // Initialize board
        initializeBoard();

        // Control buttons panel
        controlPanel.add(button1);
        controlPanel.add(button2);
        controlPanel.add(button3);
        controlPanel.add(button4);

        // Bottom panel with controls and record
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        infoPanel.add(movesLabel);
        infoPanel.add(recordLabel);
        bottomPanel.add(infoPanel, BorderLayout.SOUTH);

        // Add all panels to main panel
        mainPanel.add(shapeSizePanel, BorderLayout.NORTH);
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Set up event listeners
        setUpButtonListeners();
        setUpComboBoxListeners();
    }

    private void initializeBoard() {
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(boardRows, boardCols, 2, 2));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        boardButtons = new JToggleButton[boardRows][boardCols];

        // Create board with pegs (orange) and holes (gray)
        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                boardButtons[r][c] = new JToggleButton();
                boardButtons[r][c].setPreferredSize(new Dimension(50, 50));

                // Set colors based on position (simulate a board pattern)
                if ((r == boardRows/2 && c == boardCols/2) ||
                        (r < 2 && c < 2) ||
                        (r < 2 && c >= boardCols-2) ||
                        (r >= boardRows-2 && c < 2) ||
                        (r >= boardRows-2 && c >= boardCols-2)) {
                    // Empty positions
                    boardButtons[r][c].setEnabled(false);
                    boardButtons[r][c].setBackground(Color.DARK_GRAY);
                    boardButtons[r][c].setSelected(false);
                } else {
                    // Peg positions
                    boardButtons[r][c].setBackground(Color.ORANGE);
                    boardButtons[r][c].setSelected(true);
                }

                // Add action listener for board buttons
                final int row = r, col = c;
                boardButtons[r][c].addActionListener(e -> {
                    handleBoardClick(row, col);
                });

                boardPanel.add(boardButtons[r][c]);
            }
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void handleBoardClick(int row, int col) {
        // Toggle the button state and update color
        if (boardButtons[row][col].isSelected()) {
            boardButtons[row][col].setSelected(false);
            boardButtons[row][col].setBackground(Color.LIGHT_GRAY);
        } else {
            boardButtons[row][col].setSelected(true);
            boardButtons[row][col].setBackground(Color.ORANGE);
        }
        updateMovesLabel();
    }

    private void updateMovesLabel() {
        // Simulate counting moves
        int moves = Integer.parseInt(movesLabel.getText().replace("Moves: ", ""));
        movesLabel.setText("Moves: " + (moves + 1));
    }

    public void setUpButtonListeners() {
        // Replay button
        button1.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Replay feature will be implemented later");
            // Reset moves counter for demo
            movesLabel.setText("Moves: 0");
        });

        // New Game button
        button2.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(frame,
                    "Start a new game?", "New Game", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                // Reset board and moves
                initializeBoard();
                movesLabel.setText("Moves: 0");
                updateRecordLabel();
            }
        });

        // Autoplay button
        button3.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Autoplay feature will be implemented later");
            // Simulate some moves for demo
            for (int i = 0; i < 5; i++) {
                updateMovesLabel();
            }
        });

        // Randomize button
        button4.addActionListener(e -> {
            // Randomize the board state
            for (int r = 0; r < boardRows; r++) {
                for (int c = 0; c < boardCols; c++) {
                    if (boardButtons[r][c].isEnabled()) {
                        boolean randomState = Math.random() > 0.5;
                        boardButtons[r][c].setSelected(randomState);
                        boardButtons[r][c].setBackground(randomState ? Color.ORANGE : Color.LIGHT_GRAY);
                    }
                }
            }
            movesLabel.setText("Moves: 0");
        });

        // Custom size apply button
        customSizeButton.addActionListener(e -> {
            try {
                int newRows = Integer.parseInt(customRows.getText());
                int newCols = Integer.parseInt(customCols.getText());

                if (newRows >= 3 && newRows <= 15 && newCols >= 3 && newCols <= 15) {
                    boardRows = newRows;
                    boardCols = newCols;
                    initializeBoard();
                    movesLabel.setText("Moves: 0");
                    frame.revalidate();
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter values between 3 and 15");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numbers");
            }
        });
    }

    private void setUpComboBoxListeners() {
        // Shape combo box listener
        shapeComboBox.addActionListener(e -> {
            currentShape = (String) shapeComboBox.getSelectedItem();
            updateBoardForShape();
        });

        // Size combo box listener
        sizeComboBox.addActionListener(e -> {
            String size = (String) sizeComboBox.getSelectedItem();
            switch (size) {
                case "Small":
                    boardRows = 5;
                    boardCols = 5;
                    break;
                case "Medium":
                    boardRows = 7;
                    boardCols = 7;
                    break;
                case "Large":
                    boardRows = 9;
                    boardCols = 9;
                    break;
                case "Custom":
                    // Enable custom inputs
                    customRows.setEnabled(true);
                    customCols.setEnabled(true);
                    customSizeButton.setEnabled(true);
                    return; // Don't update board yet
            }
            // Disable custom inputs for non-custom sizes
            customRows.setEnabled(false);
            customCols.setEnabled(false);
            customSizeButton.setEnabled(false);
            updateBoardForShape();
        });
    }

    private void updateBoardForShape() {
        switch (currentShape) {
            case "English":
                // Standard English board pattern
                boardRows = 7;
                boardCols = 7;
                break;
            case "Hexagonal":
                // Hexagonal pattern (odd dimensions)
                boardRows = 9;
                boardCols = 9;
                break;
            case "Diamond":
                // Diamond pattern (square)
                boardRows = 9;
                boardCols = 9;
                break;
        }
        initializeBoard();
        movesLabel.setText("Moves: 0");
    }

    private void updateRecordLabel() {
        // For demo, set a random record
        int currentRecord = (int) (Math.random() * 20) + 10;
        recordLabel.setText("Record: " + currentRecord + " moves");
    }

    // Main method to test the GUI
    public static void main(String[] args) {
        // Run on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> {
            GUI pegSolitaireGUI = new GUI(800, 700);
            pegSolitaireGUI.setUpGUI();
        });
    }
}