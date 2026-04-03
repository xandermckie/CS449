package com.solitaire;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main application window for Peg Solitaire — Sprint 4.
 * Soviet Constructivist theme: deep pink, blush, black.
 *
 * Sprint 4 additions:
 *  - "● RECORD" toggle: records every move and randomize to a .txt file.
 *  - "▶ REPLAY" button: loads a recording and replays it step by step.
 *
 * Bug fixes vs Sprint 3:
 *  - Autoplay now records each automated move via recordMove() in the timer tick.
 *  - Randomize passes the board to recordRandomize() so the exact post-shuffle
 *    state is stored and restored perfectly on replay.
 */
public class GUI extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────────────
    static final Color BLACK     = new Color(10,  8,  10);
    static final Color DEEP_RED  = new Color(160, 50, 110);
    static final Color CRIMSON   = new Color(210, 80, 140);
    static final Color ROSE      = new Color(235, 130, 175);
    static final Color BLUSH     = new Color(245, 175, 205);
    static final Color PALE_PINK = new Color(252, 225, 238);
    static final Color OFF_WHITE = new Color(255, 245, 250);
    static final Color REC_ON    = new Color(220, 40,  40);

    private static final int WINDOW_WIDTH      = 860;
    private static final int WINDOW_HEIGHT     = 700;
    private static final int AUTOPLAY_DELAY_MS = 400;
    private static final int REPLAY_DELAY_MS   = 600;

    // ── Controls ──────────────────────────────────────────────────────────────────
    private JRadioButton englishRadio, hexagonRadio, diamondRadio;
    private JRadioButton manualRadio, automatedRadio;
    private JSpinner     sizeSpinner;
    private JLabel       statusLabel, pegCountLabel, modeLabel;
    private JButton      recordBtn;

    // ── Game state ────────────────────────────────────────────────────────────────
    private Game       currentGame;
    private BoardPanel boardPanel;
    private Timer      autoplayTimer;

    // ── Recording state ───────────────────────────────────────────────────────────
    private GameRecord currentRecord;
    private boolean    isRecording = false;

    // ── Replay state ──────────────────────────────────────────────────────────────
    private GameReplayer replayer;
    private Timer        replayTimer;

    // ── Constructor ───────────────────────────────────────────────────────────────
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

    // ── Header ────────────────────────────────────────────────────────────────────
    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), ht = getHeight();
                g2.setColor(BLACK);    g2.fillRect(0,0,w,ht);
                g2.setColor(DEEP_RED);
                g2.fillPolygon(new int[]{0,w/2+60,w/2-20,0}, new int[]{0,0,ht,ht}, 4);
                g2.setColor(ROSE);  g2.setStroke(new BasicStroke(4f));
                g2.drawLine(w/2-20,0,w/2-60,ht);
                g2.setColor(BLUSH); g2.fillRect(0,ht-5,w,5);
                g2.setColor(new Color(245,175,205,60)); g2.fillOval(w-110,-30,120,120);
                g2.setColor(new Color(210,80,140,80));  g2.fillOval(w-80,10,70,70);
            }
        };
        h.setPreferredSize(new Dimension(WINDOW_WIDTH, 80)); h.setOpaque(false);
        JLabel title = new JLabel("PEG  SOLITAIRE") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,120)); g2.setFont(getFont()); g2.drawString(getText(),3,getHeight()-15);
                g2.setColor(OFF_WHITE); g2.drawString(getText(),0,getHeight()-18);
            }
        };
        title.setFont(new Font("Serif", Font.BOLD, 34));
        title.setBorder(BorderFactory.createEmptyBorder(0,28,0,0));
        JLabel sub = new JLabel("MANUAL  ·  AUTOMATED  ·  RECORD  ·  REPLAY");
        sub.setFont(new Font("SansSerif", Font.BOLD, 10));
        sub.setForeground(BLUSH);
        sub.setBorder(BorderFactory.createEmptyBorder(0,0,0,24));
        h.add(title, BorderLayout.WEST);
        h.add(sub,   BorderLayout.EAST);
        return h;
    }

    // ── Side panel ────────────────────────────────────────────────────────────────
    private JPanel createSidePanel() {
        JPanel side = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(18,8,16)); g2.fillRect(0,0,w,h);
                g2.setColor(DEEP_RED); g2.fillRect(w-4,0,4,h);
                g2.setColor(new Color(160,50,110,100));
                g2.fillRect(0,140,w,3); g2.fillRect(0,300,w,3); g2.fillRect(0,420,w,3);
                g2.setColor(ROSE);  g2.fillRect(12,12,18,18);
                g2.setColor(BLUSH); g2.fillRect(16,16,10,10);
            }
        };
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(195,0));
        side.setBorder(BorderFactory.createEmptyBorder(16,14,16,14));

        side.add(Box.createVerticalStrut(44));
        side.add(makeLabel("BOARD SIZE", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(6));
        sizeSpinner = new JSpinner(new SpinnerNumberModel(7,3,15,2));
        sizeSpinner.setMaximumSize(new Dimension(160,38));
        styleSpinner(sizeSpinner);
        side.add(sizeSpinner);

        side.add(Box.createVerticalStrut(22)); side.add(makeDivider());
        side.add(Box.createVerticalStrut(12));
        side.add(makeLabel("BOARD TYPE", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(10));
        englishRadio = makeRadio("ENGLISH"); hexagonRadio = makeRadio("HEXAGON"); diamondRadio = makeRadio("DIAMOND");
        englishRadio.setSelected(true);
        ButtonGroup bg = new ButtonGroup(); bg.add(englishRadio); bg.add(hexagonRadio); bg.add(diamondRadio);
        side.add(englishRadio); side.add(Box.createVerticalStrut(6));
        side.add(hexagonRadio); side.add(Box.createVerticalStrut(6));
        side.add(diamondRadio);

        side.add(Box.createVerticalStrut(22)); side.add(makeDivider());
        side.add(Box.createVerticalStrut(12));
        side.add(makeLabel("GAME MODE", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(10));
        manualRadio = makeRadio("MANUAL"); automatedRadio = makeRadio("AUTOMATED");
        manualRadio.setSelected(true);
        ButtonGroup mg = new ButtonGroup(); mg.add(manualRadio); mg.add(automatedRadio);
        side.add(manualRadio); side.add(Box.createVerticalStrut(6)); side.add(automatedRadio);

        side.add(Box.createVerticalStrut(22)); side.add(makeDivider());
        side.add(Box.createVerticalStrut(12));
        side.add(makeLabel("STATUS", BLUSH, 10, Font.BOLD));
        side.add(Box.createVerticalStrut(8));
        statusLabel = new JLabel("<html>Press<br>NEW GAME<br>to start</html>");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(PALE_PINK); statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(statusLabel); side.add(Box.createVerticalStrut(6));
        modeLabel = new JLabel(" ");
        modeLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        modeLabel.setForeground(ROSE); modeLabel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(modeLabel); side.add(Box.createVerticalStrut(4));
        pegCountLabel = new JLabel(" ");
        pegCountLabel.setFont(new Font("Serif", Font.BOLD, 20));
        pegCountLabel.setForeground(ROSE); pegCountLabel.setAlignmentX(LEFT_ALIGNMENT);
        side.add(pegCountLabel);
        return side;
    }

    // ── Board area ────────────────────────────────────────────────────────────────
    private JScrollPane createBoardArea() {
        boardPanel = new BoardPanel();
        boardPanel.setMoveListener((fromRow, fromCol, toRow, toCol) -> {
            if (currentGame == null || !(currentGame instanceof ManualGame)) return;
            boolean moved = currentGame.makeMove(fromRow, fromCol, toRow, toCol);
            if (moved) {
                // Record the move if recording is active
                if (isRecording && currentRecord != null)
                    currentRecord.recordMove(fromRow, fromCol, toRow, toCol);
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
        scroll.getViewport().setBackground(new Color(18,5,8));
        scroll.setBorder(new MatteBorder(0,4,0,0,DEEP_RED));
        return scroll;
    }

    // ── Button panel ──────────────────────────────────────────────────────────────
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(18,8,16)); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(DEEP_RED); g2.fillRect(0,0,getWidth(),4);
                g2.setColor(BLUSH);    g2.fillRect(0,4,getWidth(),2);
            }
        };
        panel.setOpaque(false);

        JButton newGameBtn   = makeSovietButton("NEW GAME",  CRIMSON,                OFF_WHITE);
        JButton autoplayBtn  = makeSovietButton("AUTOPLAY",  DEEP_RED,               OFF_WHITE);
        JButton randomizeBtn = makeSovietButton("RANDOMIZE", BLACK,                  ROSE);
        randomizeBtn.setBorder(BorderFactory.createLineBorder(ROSE, 2));
        recordBtn            = makeSovietButton("● RECORD",  new Color(160,20,20),   OFF_WHITE);
        JButton replayBtn    = makeSovietButton("▶ REPLAY",  BLACK,                  BLUSH);
        replayBtn.setBorder(BorderFactory.createLineBorder(BLUSH, 2));
        JButton exitBtn      = makeSovietButton("EXIT",      BLACK, new Color(180,180,180));
        exitBtn.setBorder(BorderFactory.createLineBorder(new Color(180,180,180), 2));

        newGameBtn.addActionListener(e -> startNewGame());

        // ── Autoplay — FIX: record each move in the timer tick ────────────────────
        autoplayBtn.addActionListener(e -> {
            if (currentGame == null) return;
            if (autoplayTimer != null && autoplayTimer.isRunning()) {
                autoplayTimer.stop();
                statusLabel.setText("<html>Autoplay<br>paused.</html>");
                return;
            }
            // Switch to automated game if currently manual
            if (currentGame instanceof ManualGame) {
                int size     = currentGame.getBoard().getSize();
                BoardType bt = currentGame.getBoard().getType();
                currentGame  = new AutomatedGame(size, bt);
                // If recording, start a fresh record for the automated game
                if (isRecording)
                    currentRecord = new GameRecord(size, bt, "Automated");
                boardPanel.setBoard(currentGame.getBoard());
                modeLabel.setText("MODE: AUTO");
            }
            autoplayTimer = new Timer(AUTOPLAY_DELAY_MS, ev -> {
                int pegsBefore = currentGame.getPegCount();
                boolean moved  = ((AutomatedGame) currentGame).makeAutoMove();

                if (!moved || currentGame.isGameOver()) {
                    autoplayTimer.stop();
                    // Record final state if a last move was made
                    if (moved && isRecording && currentRecord != null) {
                        // find what move was made by comparing peg counts
                        // We detect the last move coordinates via Board scan
                        recordLastAutoMove(pegsBefore);
                    }
                    if (isRecording && currentRecord != null) {
                        currentRecord.recordEnd(currentGame.getPegCount(),
                                currentGame.getPerformanceRating());
                    }
                    boardPanel.repaint();
                    updateStatus();
                    handleGameOver();
                } else {
                    // FIX: record this automated move
                    if (isRecording && currentRecord != null) {
                        recordLastAutoMove(pegsBefore);
                    }
                    boardPanel.repaint();
                    updateStatus();
                }
            });
            autoplayTimer.start();
            statusLabel.setText("<html>Autoplay<br>running...</html>");
        });

        // ── Randomize — FIX: pass board to recordRandomize so snapshot is stored ──
        randomizeBtn.addActionListener(e -> {
            if (currentGame == null) return;
            if (autoplayTimer != null) autoplayTimer.stop();
            currentGame.randomize();
            // Pass the board AFTER randomization so the snapshot is correct
            if (isRecording && currentRecord != null)
                currentRecord.recordRandomize(currentGame.getBoard());
            boardPanel.repaint();
            updateStatus();
            statusLabel.setText("<html>Board<br>randomized!</html>");
        });

        recordBtn.addActionListener(e -> toggleRecording());
        replayBtn.addActionListener(e -> startReplay());
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(newGameBtn); panel.add(autoplayBtn); panel.add(randomizeBtn);
        panel.add(recordBtn);  panel.add(replayBtn);   panel.add(exitBtn);
        return panel;
    }

    // ── Recording helpers ─────────────────────────────────────────────────────────

    /**
     * Scans the board to find which move was just made by the automated game.
     * We compare expected peg count (before) with current state — the move that
     * caused one peg to disappear and another to appear can be identified by
     * looking at the last EMPTY hole that now has a peg (destination) and the
     * source that is now empty.
     *
     * Simpler approach: AutomatedGame exposes getLastMove() for this purpose.
     */
    private void recordLastAutoMove(int pegsBefore) {
        if (!(currentGame instanceof AutomatedGame)) return;
        int[] last = ((AutomatedGame) currentGame).getLastMove();
        if (last != null && currentRecord != null)
            currentRecord.recordMove(last[0], last[1], last[2], last[3]);
    }

    private void toggleRecording() {
        if (currentGame == null) {
            JOptionPane.showMessageDialog(this,
                    "Please start a game before recording.",
                    "No game active", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!isRecording) {
            isRecording   = true;
            currentRecord = new GameRecord(
                    currentGame.getBoard().getSize(),
                    currentGame.getBoard().getType(),
                    currentGame.getModeName());
            recordBtn.setBackground(REC_ON);
            statusLabel.setText("<html>● REC<br>active</html>");
        } else {
            stopRecordingAndSave();
        }
    }

    private void stopRecordingAndSave() {
        isRecording = false;
        recordBtn.setBackground(new Color(160,20,20));
        if (currentRecord == null || currentRecord.getActionCount() == 0) {
            statusLabel.setText("<html>Recording<br>empty.</html>");
            currentRecord = null;
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Recording");
        chooser.setSelectedFile(new File("game_recording.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = chooser.getSelectedFile().getAbsolutePath();
                if (!path.endsWith(".txt")) path += ".txt";
                currentRecord.saveToFile(path);
                statusLabel.setText("<html>Saved!<br>"
                        + chooser.getSelectedFile().getName() + "</html>");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        currentRecord = null;
    }

    // ── Replay ────────────────────────────────────────────────────────────────────

    private void startReplay() {
        if (autoplayTimer != null) autoplayTimer.stop();
        if (replayTimer   != null) replayTimer.stop();
        if (isRecording) stopRecordingAndSave();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Recording");
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            GameRecord record = GameRecord.loadFromFile(
                    chooser.getSelectedFile().getAbsolutePath());
            replayer    = new GameReplayer(record);
            currentGame = replayer.getGame();
            boardPanel.setBoard(currentGame.getBoard());
            modeLabel.setText("MODE: REPLAY (" + record.getModeName() + ")");
            statusLabel.setText("<html>Replaying...<br>" + record.getModeName() + "</html>");
            updateStatus();
            pack();

            replayTimer = new Timer(REPLAY_DELAY_MS, ev -> {
                if (replayer.hasNext()) {
                    GameRecord.RecordedAction action = replayer.stepForward();
                    boardPanel.repaint();
                    updateStatus();
                    if (action != null && action.type == GameRecord.RecordedAction.Type.RANDOMIZE)
                        statusLabel.setText("<html>Replaying...<br>Randomized!</html>");
                } else {
                    ((Timer) ev.getSource()).stop();
                    boardPanel.repaint();
                    int    pegs   = replayer.getFinalPegCount();
                    String rating = replayer.getFinalRating();
                    statusLabel.setText("<html>Replay<br>complete!</html>");
                    JOptionPane.showMessageDialog(this,
                            "<html><b>Replay Complete</b><br><br>"
                                    + "Pegs remaining: <b>" + pegs + "</b><br>"
                                    + "Rating: <b>" + rating + "</b></html>",
                            "Replay Finished", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            replayTimer.start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load: " + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Game logic ────────────────────────────────────────────────────────────────

    private void startNewGame() {
        if (autoplayTimer != null) autoplayTimer.stop();
        if (replayTimer   != null) replayTimer.stop();
        if (isRecording) stopRecordingAndSave();

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
        if (isRecording && currentRecord != null) {
            currentRecord.recordEnd(currentGame.getPegCount(),
                    currentGame.getPerformanceRating());
            stopRecordingAndSave();
        }
        String rating = currentGame.getPerformanceRating();
        int    pegs   = currentGame.getPegCount();
        String mode   = currentGame.getModeName();
        statusLabel.setText("<html><b>GAME<br>OVER</b></html>");

        JPanel msg = new JPanel(new BorderLayout(0,12));
        msg.setBackground(new Color(18,8,16));
        msg.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));
        JLabel title = new JLabel("GAME OVER — " + mode.toUpperCase());
        title.setFont(new Font("Serif", Font.BOLD, 18)); title.setForeground(ROSE);
        JLabel body = new JLabel(
                "<html><font color='#F0A0AA'>Pegs remaining: <b>" + pegs + "</b><br>"
                        + "Rating: <b>" + rating + "</b><br><br>"
                        + "<font color='#F5EEEB'>Start a new game?</font></font></html>");
        body.setFont(new Font("SansSerif", Font.PLAIN, 13));
        msg.add(title, BorderLayout.NORTH); msg.add(body, BorderLayout.CENTER);

        UIManager.put("OptionPane.background",        new Color(18,8,16));
        UIManager.put("Panel.background",             new Color(18,8,16));
        UIManager.put("OptionPane.messageForeground", OFF_WHITE);
        UIManager.put("Button.background",            CRIMSON);
        UIManager.put("Button.foreground",            OFF_WHITE);

        int choice = JOptionPane.showConfirmDialog(this, msg, "GAME OVER",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) startNewGame();
    }

    private void updateStatus() {
        if (currentGame != null) pegCountLabel.setText(currentGame.getPegCount() + " pegs");
    }

    // ── Accessors ─────────────────────────────────────────────────────────────────
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
        l.setForeground(color); l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(DEEP_RED); sep.setBackground(DEEP_RED);
        sep.setMaximumSize(new Dimension(160,2));
        return sep;
    }
    private JRadioButton makeRadio(String text) {
        JRadioButton rb = new JRadioButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(18,8,16)); g2.fillRect(0,0,getWidth(),getHeight());
                int sq=12, oy=(getHeight()-sq)/2;
                g2.setColor(DEEP_RED); g2.fillRect(2,oy,sq,sq);
                if (isSelected()) { g2.setColor(BLUSH); g2.fillRect(5,oy+3,6,6); }
                g2.setColor(ROSE); g2.setStroke(new BasicStroke(1.5f)); g2.drawRect(2,oy,sq,sq);
                g2.setColor(isSelected() ? OFF_WHITE : PALE_PINK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),sq+10,(getHeight()+fm.getAscent()-fm.getDescent())/2);
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
        JComponent ed = spinner.getEditor();
        if (ed instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) ed).getTextField();
            tf.setBackground(DEEP_RED); tf.setForeground(OFF_WHITE);
            tf.setCaretColor(OFF_WHITE); tf.setFont(new Font("Serif", Font.BOLD, 20));
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
        }
        spinner.setBorder(BorderFactory.createLineBorder(ROSE,2));
        spinner.setAlignmentX(LEFT_ALIGNMENT);
    }
    private JButton makeSovietButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()?bg.darker():getModel().isRollover()?bg.brighter():bg);
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
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(108,40));
        btn.setFocusPainted(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}