package com.solitaire;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Main application window for Peg Solitaire — Sprint 3.
 * Themed after Soviet Constructivist design: bold geometry, deep pink, blush, black.
 *
 * UI layer only. Game logic handled by {@link ManualGame} and {@link AutomatedGame}.
 *
 * Sprint 3 additions:
 *  - Game mode radio buttons (Manual / Automated)
 *  - Autoplay button: steps through automated moves with a timer
 *  - Randomize button: shuffles the board during a manual game
 */
public class GUI extends JFrame {

    // ── Colour palette ───────────────────────────────────────────────────────────
    static final Color BLACK     = new Color(10,  8,  10);
    static final Color DEEP_RED  = new Color(160, 50, 110);
    static final Color CRIMSON   = new Color(210, 80, 140);
    static final Color ROSE      = new Color(235, 130, 175);
    static final Color BLUSH     = new Color(245, 175, 205);
    static final Color PALE_PINK = new Color(252, 225, 238);
    static final Color OFF_WHITE = new Color(255, 245, 250);

    private static final int WINDOW_WIDTH  = 860;
    private static final int WINDOW_HEIGHT = 680;
    private static final int AUTOPLAY_DELAY_MS = 400;

    // ── UI controls ──────────────────────────────────────────────────────────────
    private JRadioButton englishRadio, hexagonRadio, diamondRadio;
    private JRadioButton manualRadio, automatedRadio;
    private JSpinner     sizeSpinner;
    private JLabel       statusLabel, pegCountLabel, modeLabel;

    // ── Game state ───────────────────────────────────────────────────────────────
    private Game       currentGame;
    private BoardPanel boardPanel;
    private Timer      autoplayTimer;

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
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(BLACK); g2.fillRect(0, 0, w, h);
                g2.setColor(DEEP_RED);
                g2.fillPolygon(new int[]{0, w/2+60, w/2-20, 0}, new int[]{0, 0, h, h}, 4);
                g2.setColor(ROSE); g2.setStroke(new BasicStroke(4f));
                g2.drawLine(w/2-20, 0, w/2-60, h);
                g2.setColor(BLUSH); g2.fillRect(0, h-5, w, 5);
                g2.setColor(new Color(245,175,205,60)); g2.fillOval(w-110,-30,120,120);
                g2.setColor(new Color(210,80,140,80));  g2.fillOval(w-80,10,70,70);
            }
        };
        header.setPreferredSize(new Dimension(WINDOW_WIDTH, 80));
        header.setOpaque(false);

        JLabel title = new JLabel("PEG  SOLITAIRE") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,120)); g2.setFont(getFont()); g2.drawString(getText(), 3, getHeight()-15);
                g2.setColor(OFF_WHITE); g2.drawString(getText(), 0, getHeight()-18);
            }
        };
        title.setFont(new Font("Serif", Font.BOLD, 34));
        title.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 0));

        JLabel sub = new JLabel("MANUAL  ·  AUTOMATED  ·  RANDOMIZE");
        sub.setFont(new Font("SansSerif", Font.BOLD, 10));
        sub.setForeground(BLUSH);
        sub.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));

        header.add(title, BorderLayout.WEST);
        header.add(sub,   BorderLayout.EAST);
        return header;
    }

    // ── Side panel ───────────────────────────────────────────────────────────────

    private JPanel createSidePanel() {
        JPanel side = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(18, 8, 16)); g2.fillRect(0,0,w,h);
                g2.setColor(DEEP_RED); g2.fillRect(w-4,0,4,h);
                g2.setColor(new Color(160,50,110,100));
                g2.fillRect(0,140,w,3); g2.fillRect(0,300,w,3); g2.fillRect(0,420,w,3);
                g2.setColor(ROSE); g2.fillRect(12,12,18,18);
                g2.setColor(BLUSH); g2.fillRect(16,16,10,10);
            }
        };
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(195, 0));
        side.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));

        // Board size
        side.add(Box.createVerticalStrut(44));
        side.add(makeLabel("BOARD SIZE", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(6));
        sizeSpinner = new JSpinner(new SpinnerNumberModel(7, 3, 15, 2));
        sizeSpinner.setMaximumSize(new Dimension(160, 38));
        styleSpinner(sizeSpinner);
        side.add(sizeSpinner);

        // Board type
        side.add(Box.createVerticalStrut(22));
        side.add(makeDivider());
        side.add(Box.createVerticalStrut(12));
        side.add(makeLabel("BOARD TYPE", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(10));

        englishRadio  = makeRadio("ENGLISH");
        hexagonRadio  = makeRadio("HEXAGON");
        diamondRadio  = makeRadio("DIAMOND");
        englishRadio.setSelected(true);
        ButtonGroup boardGroup = new ButtonGroup();
        boardGroup.add(englishRadio); boardGroup.add(hexagonRadio); boardGroup.add(diamondRadio);
        side.add(englishRadio); side.add(Box.createVerticalStrut(6));
        side.add(hexagonRadio); side.add(Box.createVerticalStrut(6));
        side.add(diamondRadio);

        // Game mode
        side.add(Box.createVerticalStrut(22));
        side.add(makeDivider());
        side.add(Box.createVerticalStrut(12));
        side.add(makeLabel("GAME MODE", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(10));

        manualRadio    = makeRadio("MANUAL");
        automatedRadio = makeRadio("AUTOMATED");
        manualRadio.setSelected(true);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(manualRadio); modeGroup.add(automatedRadio);
        side.add(manualRadio); side.add(Box.createVerticalStrut(6));
        side.add(automatedRadio);

        // Status
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

        side.add(Box.createVerticalStrut(6));

        modeLabel = new JLabel(" ");
        modeLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        modeLabel.setForeground(ROSE);
        modeLabel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(modeLabel);

        side.add(Box.createVerticalStrut(4));

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
            if (currentGame == null || !(currentGame instanceof ManualGame)) return;
            boolean moved = currentGame.makeMove(fromRow, fromCol, toRow, toCol);
            if (moved) {
                boardPanel.repaint();
                updateStatus();
                if (currentGame.isGameOver()) handleGameOver();
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(18, 8, 16)); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(DEEP_RED); g2.fillRect(0,0,getWidth(),4);
                g2.setColor(BLUSH);    g2.fillRect(0,4,getWidth(),2);
            }
        };
        panel.setOpaque(false);

        JButton newGameBtn  = makeSovietButton("NEW GAME",  CRIMSON,  OFF_WHITE);
        JButton autoplayBtn = makeSovietButton("AUTOPLAY",  DEEP_RED, OFF_WHITE);
        JButton randomizeBtn= makeSovietButton("RANDOMIZE", BLACK,    ROSE);
        randomizeBtn.setBorder(BorderFactory.createLineBorder(ROSE, 2));
        JButton exitBtn     = makeSovietButton("EXIT",      BLACK,    BLUSH);
        exitBtn.setBorder(BorderFactory.createLineBorder(BLUSH, 2));

        newGameBtn.addActionListener(e -> startNewGame());

        // Autoplay: step through automated moves with a timer
        autoplayBtn.addActionListener(e -> {
            if (currentGame == null) return;
            if (autoplayTimer != null && autoplayTimer.isRunning()) {
                autoplayTimer.stop();
                statusLabel.setText("<html>Autoplay<br>paused.</html>");
                return;
            }
            // Switch to automated mode if in manual
            if (currentGame instanceof ManualGame) {
                int size = currentGame.getBoard().getSize();
                BoardType type = currentGame.getBoard().getType();
                currentGame = new AutomatedGame(size, type);
                boardPanel.setBoard(currentGame.getBoard());
                modeLabel.setText("MODE: AUTO");
            }
            autoplayTimer = new Timer(AUTOPLAY_DELAY_MS, ev -> {
                if (currentGame.isGameOver() || !((AutomatedGame) currentGame).makeAutoMove()) {
                    autoplayTimer.stop();
                    boardPanel.repaint();
                    updateStatus();
                    handleGameOver();
                } else {
                    boardPanel.repaint();
                    updateStatus();
                }
            });
            autoplayTimer.start();
            statusLabel.setText("<html>Autoplay<br>running...</html>");
        });

        // Randomize: shuffle board during manual game
        randomizeBtn.addActionListener(e -> {
            if (currentGame == null) return;
            if (autoplayTimer != null) autoplayTimer.stop();
            currentGame.randomize();
            boardPanel.repaint();
            updateStatus();
            statusLabel.setText("<html>Board<br>randomized!</html>");
        });

        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(newGameBtn);
        panel.add(autoplayBtn);
        panel.add(randomizeBtn);
        panel.add(exitBtn);
        return panel;
    }

    // ── Game logic ───────────────────────────────────────────────────────────────

    private void startNewGame() {
        if (autoplayTimer != null) autoplayTimer.stop();

        int size = (int) sizeSpinner.getValue();
        if (size % 2 == 0) size++;

        BoardType type = hexagonRadio.isSelected() ? BoardType.HEXAGON
                : diamondRadio.isSelected()  ? BoardType.DIAMOND
                : BoardType.ENGLISH;

        if (automatedRadio.isSelected()) {
            currentGame = new AutomatedGame(size, type);
            modeLabel.setText("MODE: AUTO");
        } else {
            currentGame = new ManualGame(size, type);
            modeLabel.setText("MODE: MANUAL");
        }

        boardPanel.setBoard(currentGame.getBoard());
        statusLabel.setText("<html>Game<br>started!</html>");
        updateStatus();
        pack();
    }

    private void handleGameOver() {
        if (autoplayTimer != null) autoplayTimer.stop();

        String rating = currentGame.getPerformanceRating();
        int    pegs   = currentGame.getPegCount();
        String mode   = currentGame.getModeName();

        statusLabel.setText("<html><b>GAME<br>OVER</b></html>");

        JPanel msg = new JPanel(new BorderLayout(0, 12));
        msg.setBackground(new Color(18, 8, 16));
        msg.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("GAME OVER — " + mode.toUpperCase());
        title.setFont(new Font("Serif", Font.BOLD, 18));
        title.setForeground(ROSE);

        JLabel body = new JLabel(
                "<html><font color='#F0A0AA'>Pegs remaining: <b>" + pegs + "</b><br>"
                        + "Rating: <b>" + rating + "</b><br><br>"
                        + "<font color='#F5EEEB'>Start a new game?</font></font></html>");
        body.setFont(new Font("SansSerif", Font.PLAIN, 13));

        msg.add(title, BorderLayout.NORTH);
        msg.add(body,  BorderLayout.CENTER);

        UIManager.put("OptionPane.background",        new Color(18, 8, 16));
        UIManager.put("Panel.background",             new Color(18, 8, 16));
        UIManager.put("OptionPane.messageForeground", OFF_WHITE);
        UIManager.put("Button.background",            CRIMSON);
        UIManager.put("Button.foreground",            OFF_WHITE);

        int choice = JOptionPane.showConfirmDialog(
                this, msg, "GAME OVER",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) startNewGame();
    }

    private void updateStatus() {
        if (currentGame != null)
            pegCountLabel.setText(currentGame.getPegCount() + " pegs");
    }

    // ── Accessors ────────────────────────────────────────────────────────────────

    public String getSelectedBoardType() {
        if (hexagonRadio.isSelected()) return "Hexagon";
        if (diamondRadio.isSelected())  return "Diamond";
        return "English";
    }

    public int getSelectedBoardSize() { return (int) sizeSpinner.getValue(); }

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
        sep.setForeground(DEEP_RED); sep.setBackground(DEEP_RED);
        sep.setMaximumSize(new Dimension(160, 2));
        return sep;
    }

    private JRadioButton makeRadio(String text) {
        JRadioButton rb = new JRadioButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(18,8,16)); g2.fillRect(0,0,getWidth(),getHeight());
                int sq = 12, oy = (getHeight()-sq)/2;
                g2.setColor(DEEP_RED); g2.fillRect(2,oy,sq,sq);
                if (isSelected()) { g2.setColor(BLUSH); g2.fillRect(5,oy+3,6,6); }
                g2.setColor(ROSE); g2.setStroke(new BasicStroke(1.5f)); g2.drawRect(2,oy,sq,sq);
                g2.setColor(isSelected() ? OFF_WHITE : PALE_PINK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), sq+10, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        rb.setFont(new Font("SansSerif", Font.BOLD, 11));
        rb.setOpaque(false); rb.setFocusPainted(false);
        rb.setContentAreaFilled(false); rb.setBorderPainted(false);
        rb.setPreferredSize(new Dimension(160,28)); rb.setMaximumSize(new Dimension(160,28));
        rb.setAlignmentX(LEFT_ALIGNMENT);
        return rb;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(DEEP_RED); spinner.setForeground(OFF_WHITE); spinner.setOpaque(true);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(DEEP_RED); tf.setForeground(OFF_WHITE);
            tf.setCaretColor(OFF_WHITE); tf.setFont(new Font("Serif", Font.BOLD, 20));
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
        }
        spinner.setBorder(BorderFactory.createLineBorder(ROSE, 2));
        spinner.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton makeSovietButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                        getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRect(0,0,getWidth(),getHeight());
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,30));
                    g2.fillPolygon(new int[]{0,20,getWidth(),getWidth()-20},
                            new int[]{0,0,getHeight(),getHeight()},4);
                }
                g2.setColor(fg); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text,(getWidth()-fm.stringWidth(text))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(118, 40));
        btn.setFocusPainted(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}