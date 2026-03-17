package com.solitaire;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Main application window for Peg Solitaire.
 * Themed after Soviet Constructivist design: bold geometry, dark red, pink, black.
 *
 * UI layer only — all game logic is handled by {@link Board}.
 *
 * User Story 3: As a player, I want to start a new game using the board size and
 * type I have selected so that I can play a fresh game tailored to my preferences.
 *
 * AC 1.2: Given a game is already in progress, When the player clicks "New Game",
 * Then the current game state is discarded and a new game is started.
 */
public class GUI extends JFrame {

    // ── Constructivist colour palette ────────────────────────────────────────────
    static final Color BLACK       = new Color(10,  8,  8);
    static final Color DEEP_RED    = new Color(139, 10, 20);
    static final Color CRIMSON     = new Color(185, 20, 35);
    static final Color ROSE        = new Color(220, 80, 95);
    static final Color BLUSH       = new Color(240, 160, 170);
    static final Color PALE_PINK   = new Color(250, 220, 225);
    static final Color OFF_WHITE   = new Color(245, 238, 235);

    private static final int WINDOW_WIDTH  = 820;
    private static final int WINDOW_HEIGHT = 660;

    // ── UI controls ──────────────────────────────────────────────────────────────
    private JRadioButton englishRadio;
    private JRadioButton hexagonRadio;
    private JRadioButton diamondRadio;
    private JSpinner     sizeSpinner;
    private JLabel       statusLabel;
    private JLabel       pegCountLabel;

    // ── Game components ──────────────────────────────────────────────────────────
    private Board      board;
    private BoardPanel boardPanel;

    // ── Constructor ──────────────────────────────────────────────────────────────
    public GUI() {
        setTitle("PEG SOLITAIRE");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BLACK);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createSidePanel(),   BorderLayout.WEST);
        add(createBoardArea(),   BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    // ── Header ───────────────────────────────────────────────────────────────────

    private JPanel createHeaderPanel() {
        // Custom painted header with constructivist diagonal geometry
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // Base black
                g2.setColor(BLACK);
                g2.fillRect(0, 0, w, h);

                // Large crimson diagonal block (top-left to mid)
                g2.setColor(DEEP_RED);
                int[] xPts = {0, w / 2 + 60, w / 2 - 20, 0};
                int[] yPts = {0, 0, h, h};
                g2.fillPolygon(xPts, yPts, 4);

                // Thinner rose accent stripe
                g2.setColor(ROSE);
                g2.setStroke(new BasicStroke(4f));
                g2.drawLine(w / 2 - 20, 0, w / 2 - 60, h);

                // Bottom border bar in blush
                g2.setColor(BLUSH);
                g2.fillRect(0, h - 5, w, 5);

                // Decorative circles (constructivist motif)
                g2.setColor(new Color(240, 160, 170, 60));
                g2.fillOval(w - 110, -30, 120, 120);
                g2.setColor(new Color(185, 20, 35, 80));
                g2.fillOval(w - 80, 10, 70, 70);
            }
        };
        header.setPreferredSize(new Dimension(WINDOW_WIDTH, 80));
        header.setOpaque(false);

        // Title label
        JLabel title = new JLabel("PEG  SOLITAIRE") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 120));
                g2.setFont(getFont());
                g2.drawString(getText(), 3, getHeight() - 15);
                // Main text
                g2.setColor(OFF_WHITE);
                g2.drawString(getText(), 0, getHeight() - 18);
            }
        };
        title.setFont(new Font("Serif", Font.BOLD, 34));
        title.setForeground(OFF_WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 0));

        // Subtitle
        JLabel sub = new JLabel("BRAINSTORM  ·  STRATEGY  ·  PRECISION");
        sub.setFont(new Font("SansSerif", Font.BOLD, 10));
        sub.setForeground(BLUSH);
        sub.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));

        header.add(title, BorderLayout.WEST);
        header.add(sub,   BorderLayout.EAST);
        return header;
    }

    // ── Side panel ───────────────────────────────────────────────────────────────

    /**
     * Left side panel with constructivist geometric styling.
     * User Story 1 & 2: Board size and type selection.
     * AC 2.1: Three board type options visible.
     */
    private JPanel createSidePanel() {
        JPanel side = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();

                g2.setColor(new Color(20, 6, 8));
                g2.fillRect(0, 0, w, h);

                // Vertical accent stripe on right edge
                g2.setColor(DEEP_RED);
                g2.fillRect(w - 4, 0, 4, h);

                // Decorative horizontal bars
                g2.setColor(new Color(139, 10, 20, 100));
                g2.fillRect(0, 130, w, 3);
                g2.fillRect(0, 300, w, 3);

                // Small corner square motif
                g2.setColor(ROSE);
                g2.fillRect(12, 12, 18, 18);
                g2.setColor(BLUSH);
                g2.fillRect(16, 16, 10, 10);
            }
        };
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(190, 0));
        side.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));

        // ── BOARD SIZE label ────────────────────────────────────────────────
        side.add(Box.createVerticalStrut(44));
        side.add(makeLabel("BOARD SIZE", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(6));

        SpinnerNumberModel spinModel = new SpinnerNumberModel(7, 3, 15, 2);
        sizeSpinner = new JSpinner(spinModel);
        sizeSpinner.setFont(new Font("Serif", Font.BOLD, 18));
        sizeSpinner.setMaximumSize(new Dimension(160, 38));
        sizeSpinner.setPreferredSize(new Dimension(160, 38));
        styleSpinner(sizeSpinner);
        side.add(sizeSpinner);

        // ── BOARD TYPE section ──────────────────────────────────────────────
        side.add(Box.createVerticalStrut(22));
        side.add(makeDivider());
        side.add(Box.createVerticalStrut(12));
        side.add(makeLabel("BOARD TYPE", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(10));

        englishRadio = makeRadio("ENGLISH");
        hexagonRadio = makeRadio("HEXAGON");
        diamondRadio = makeRadio("DIAMOND");
        englishRadio.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(englishRadio);
        group.add(hexagonRadio);
        group.add(diamondRadio);

        side.add(englishRadio);
        side.add(Box.createVerticalStrut(6));
        side.add(hexagonRadio);
        side.add(Box.createVerticalStrut(6));
        side.add(diamondRadio);

        // ── STATUS section ──────────────────────────────────────────────────
        side.add(Box.createVerticalStrut(22));
        side.add(makeDivider());
        side.add(Box.createVerticalStrut(12));
        side.add(makeLabel("STATUS", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(8));

        statusLabel = new JLabel("<html>Press<br>NEW GAME<br>to start</html>");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(PALE_PINK);
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(statusLabel);

        side.add(Box.createVerticalStrut(8));

        pegCountLabel = new JLabel(" ");
        pegCountLabel.setFont(new Font("Serif", Font.BOLD, 20));
        pegCountLabel.setForeground(ROSE);
        pegCountLabel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(pegCountLabel);

        return side;
    }

    // ── Board area ───────────────────────────────────────────────────────────────

    private JScrollPane createBoardArea() {
        boardPanel = new BoardPanel();

        boardPanel.setMoveListener((fromRow, fromCol, toRow, toCol) -> {
            if (board == null) return;
            boolean moved = board.tryMove(fromRow, fromCol, toRow, toCol);
            if (moved) {
                boardPanel.repaint();
                updatePegCount();
                if (board.isGameOver()) handleGameOver();
            } else {
                statusLabel.setText("<html>Invalid<br>move —<br>try again.</html>");
                boardPanel.clearSelection();
            }
        });

        JScrollPane scroll = new JScrollPane(boardPanel);
        scroll.setBackground(BLACK);
        scroll.getViewport().setBackground(new Color(18, 5, 8));
        scroll.setBorder(new MatteBorder(0, 4, 0, 0, DEEP_RED));
        return scroll;
    }

    // ── Button panel ─────────────────────────────────────────────────────────────

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(20, 6, 8));
                g2.fillRect(0, 0, w, h);
                // Top border stripe
                g2.setColor(DEEP_RED);
                g2.fillRect(0, 0, w, 4);
                g2.setColor(BLUSH);
                g2.fillRect(0, 4, w, 2);
            }
        };
        panel.setOpaque(false);

        JButton newGameBtn = makeSovietButton("NEW GAME", CRIMSON, OFF_WHITE);
        JButton exitBtn    = makeSovietButton("EXIT",     BLACK,   ROSE);
        exitBtn.setBorder(BorderFactory.createLineBorder(ROSE, 2));

        newGameBtn.addActionListener(e -> startNewGame());
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(newGameBtn);
        panel.add(exitBtn);
        return panel;
    }

    // ── Game logic ───────────────────────────────────────────────────────────────

    /**
     * AC 1.1 / AC 1.2 / AC 3.1 / AC 3.2
     */
    private void startNewGame() {
        int size = (int) sizeSpinner.getValue();
        if (size % 2 == 0) size++;

        BoardType type = hexagonRadio.isSelected() ? BoardType.HEXAGON
                : diamondRadio.isSelected()  ? BoardType.DIAMOND
                : BoardType.ENGLISH;

        board = new Board(size, type);
        boardPanel.setBoard(board);
        statusLabel.setText("<html>Game<br>started!<br>Make a<br>move.</html>");
        updatePegCount();
        pack();
    }

    /**
     * AC 4.1 / AC 4.2 / AC 5.1 / AC 5.2
     */
    private void handleGameOver() {
        String rating = board.getPerformanceRating();
        int    pegs   = board.getPegCount();

        statusLabel.setText("<html><b>GAME<br>OVER</b></html>");
        pegCountLabel.setText("" + pegs + " left");

        // Style the dialog panel
        JPanel msg = new JPanel(new BorderLayout(0, 12));
        msg.setBackground(new Color(20, 6, 8));
        msg.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("NO MORE MOVES");
        title.setFont(new Font("Serif", Font.BOLD, 20));
        title.setForeground(ROSE);

        JLabel body = new JLabel(
                "<html><font color='#F0A0AA'>Pegs remaining: <b>" + pegs + "</b><br>"
                        + "Rating: <b>" + rating + "</b><br><br>"
                        + "<font color='#F5EEEB'>Start a new game?</font></font></html>");
        body.setFont(new Font("SansSerif", Font.PLAIN, 13));

        msg.add(title, BorderLayout.NORTH);
        msg.add(body,  BorderLayout.CENTER);

        UIManager.put("OptionPane.background",         new Color(20, 6, 8));
        UIManager.put("Panel.background",              new Color(20, 6, 8));
        UIManager.put("OptionPane.messageForeground",  OFF_WHITE);
        UIManager.put("Button.background",             CRIMSON);
        UIManager.put("Button.foreground",             OFF_WHITE);

        int choice = JOptionPane.showConfirmDialog(
                this, msg, "GAME OVER",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) startNewGame();
    }

    private void updatePegCount() {
        if (board != null)
            pegCountLabel.setText(board.getPegCount() + " pegs");
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public String getSelectedBoardType() {
        if (hexagonRadio.isSelected()) return "Hexagon";
        if (diamondRadio.isSelected())  return "Diamond";
        return "English";
    }

    public int getSelectedBoardSize() {
        return (int) sizeSpinner.getValue();
    }

    // ── Style helpers ─────────────────────────────────────────────────────────────

    private JLabel makeLabel(String text, Color color, int size, int style) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(DEEP_RED);
        sep.setBackground(DEEP_RED);
        sep.setMaximumSize(new Dimension(160, 2));
        return sep;
    }

    private JRadioButton makeRadio(String text) {
        JRadioButton rb = new JRadioButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 6, 8));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Custom square radio indicator
                int sq = 12;
                int oy = (getHeight() - sq) / 2;
                g2.setColor(DEEP_RED);
                g2.fillRect(2, oy, sq, sq);
                if (isSelected()) {
                    g2.setColor(BLUSH);
                    g2.fillRect(5, oy + 3, 6, 6);
                }
                g2.setColor(ROSE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(2, oy, sq, sq);

                // Label
                g2.setColor(isSelected() ? OFF_WHITE : PALE_PINK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), sq + 10, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        rb.setFont(new Font("SansSerif", Font.BOLD, 11));
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setContentAreaFilled(false);
        rb.setBorderPainted(false);
        rb.setPreferredSize(new Dimension(160, 28));
        rb.setMaximumSize(new Dimension(160, 28));
        rb.setAlignmentX(LEFT_ALIGNMENT);
        return rb;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(DEEP_RED);
        spinner.setForeground(OFF_WHITE);
        spinner.setOpaque(true);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(DEEP_RED);
            tf.setForeground(OFF_WHITE);
            tf.setCaretColor(OFF_WHITE);
            tf.setFont(new Font("Serif", Font.BOLD, 20));
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        }
        spinner.setBorder(BorderFactory.createLineBorder(ROSE, 2));
        spinner.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton makeSovietButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Geometric fill — sharp rectangle, no rounding
                g2.setColor(getModel().isPressed() ? bg.darker() :
                        getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Bold diagonal accent stripe on hover
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillPolygon(
                            new int[]{0, 20, getWidth(), getWidth() - 20},
                            new int[]{0, 0, getHeight(), getHeight()}, 4);
                }

                // Text
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(text)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, tx, ty);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}