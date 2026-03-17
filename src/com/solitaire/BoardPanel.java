package com.solitaire;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.JPanel;

/**
 * Renders the Peg Solitaire board with a Soviet Constructivist aesthetic.
 * All game logic is delegated to {@link Board}.
 */
public class BoardPanel extends JPanel {

    public interface MoveListener {
        void onMoveAttempted(int fromRow, int fromCol, int toRow, int toCol);
    }

    // ── Visual constants ─────────────────────────────────────────────────────────
    private static final int CELL     = 56;
    private static final int PEG_PAD  = 10;
    private static final int HOLE_PAD = 16;

    // Soviet palette — matches GUI
    private static final Color BG_COLOR        = new Color(18,  5,  8);
    private static final Color GRID_COLOR       = new Color(80,  10, 18);
    private static final Color HOLE_FILL        = new Color(40,  10, 15);
    private static final Color HOLE_BORDER      = new Color(139, 10, 20);
    private static final Color PEG_COLOR        = new Color(185, 20, 35);
    private static final Color PEG_HIGHLIGHT    = new Color(230, 100, 115);
    private static final Color PEG_SHADOW       = new Color(90,  5,  12);
    private static final Color SELECTED_FILL    = new Color(240, 160, 170);
    private static final Color SELECTED_RING    = new Color(250, 220, 225);

    // ── State ────────────────────────────────────────────────────────────────────
    private Board board;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private MoveListener moveListener;

    // ── Constructor ──────────────────────────────────────────────────────────────
    public BoardPanel() {
        setBackground(BG_COLOR);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });
    }

    // ── Public API ───────────────────────────────────────────────────────────────

    public void setMoveListener(MoveListener listener) { this.moveListener = listener; }

    /** AC 1.2: Discards visual state of previous game. */
    public void setBoard(Board board) {
        this.board       = board;
        this.selectedRow = -1;
        this.selectedCol = -1;
        int panelSize = board.getSize() * CELL + CELL;
        setPreferredSize(new Dimension(panelSize, panelSize));
        repaint();
    }

    public void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        repaint();
    }

    // ── Painting ─────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) {
            paintEmptyState(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,        RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,   RenderingHints.VALUE_STROKE_PURE);

        // Background
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Subtle grid lines for constructivist feel
        paintGrid(g2);

        int offset = CELL / 2;

        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                HoleState state = board.getHole(r, c);
                if (state == HoleState.INVALID) continue;

                int x = offset + c * CELL;
                int y = offset + r * CELL;
                boolean selected = (r == selectedRow && c == selectedCol);

                drawHole(g2, x, y);
                if (state == HoleState.PEG) {
                    drawPeg(g2, x, y, selected);
                }
            }
        }

        // Decorative corner marks — constructivist detail
        paintCornerMarks(g2);
    }

    private void paintEmptyState(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(139, 10, 20, 60));
        g2.setStroke(new BasicStroke(1f));
        for (int x = 0; x < getWidth(); x += 40)
            g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 40)
            g2.drawLine(0, y, getWidth(), y);

        g2.setColor(new Color(185, 20, 35, 80));
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        String msg = "SELECT SIZE & TYPE  ·  PRESS NEW GAME";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
//updated using Claude because I got lazy: I just copypasted this original part into claude and said fix it
    private void paintGrid(Graphics2D g2) {
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(0.5f));
        int offset = CELL / 2;
        if (board == null) return;
        int total = board.getSize() * CELL + CELL;
        for (int i = 0; i <= board.getSize(); i++) {
            int pos = offset + i * CELL;
            g2.drawLine(offset, pos, total - offset, pos);
            g2.drawLine(pos, offset, pos, total - offset);
        }
    }

    private void paintCornerMarks(Graphics2D g2) {
        int w = getWidth(), h = getHeight();
        int sz = 14;
        g2.setColor(new Color(185, 20, 35, 150));
        // Top-left
        g2.fillRect(4, 4, sz, 3);
        g2.fillRect(4, 4, 3, sz);
        // Top-right
        g2.fillRect(w - sz - 4, 4, sz, 3);
        g2.fillRect(w - 7,      4, 3, sz);
        // Bottom-left
        g2.fillRect(4, h - 7,       sz, 3);
        g2.fillRect(4, h - sz - 4,  3, sz);
        // Bottom-right
        g2.fillRect(w - sz - 4, h - 7,      sz, 3);
        g2.fillRect(w - 7,      h - sz - 4, 3, sz);
    }

    private void drawHole(Graphics2D g2, int x, int y) {
        int d = CELL - HOLE_PAD;
        int cx = x + d / 2;
        int cy = y + d / 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillOval(x + 2, y + 3, d, d);

        // Hole fill — dark square-ish with clipped circle
        g2.setColor(HOLE_FILL);
        g2.fillOval(x, y, d, d);

        // Inner ring
        g2.setColor(HOLE_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x, y, d, d);

        // Tiny centre dot
        g2.setColor(new Color(139, 10, 20, 80));
        g2.fillOval(cx - 3, cy - 3, 6, 6);
    }

    private void drawPeg(Graphics2D g2, int x, int y, boolean selected) {
        int d = CELL - PEG_PAD;

        Color baseColor = selected ? SELECTED_FILL    : PEG_COLOR;
        Color ringColor = selected ? SELECTED_RING    : PEG_HIGHLIGHT;
        Color shadColor = selected ? new Color(180, 80, 95) : PEG_SHADOW;

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillOval(x + 3, y + 4, d, d);

        // Main peg body
        g2.setColor(baseColor);
        g2.fillOval(x + 1, y + 1, d, d);

        // Highlight arc (top-left shine — constructivist gloss)
        GradientPaint shine = new GradientPaint(
                x + 1,     y + 1,     new Color(255, 255, 255, selected ? 100 : 70),
                x + d / 2, y + d / 2, new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fillOval(x + 1, y + 1, d, d);

        // Outer ring
        g2.setPaint(null);
        g2.setColor(ringColor);
        g2.setStroke(new BasicStroke(selected ? 2.5f : 1.8f));
        g2.drawOval(x + 1, y + 1, d, d);

        // Selected: extra outer pulse ring
        if (selected) {
            g2.setColor(new Color(250, 220, 225, 120));
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(x - 2, y - 2, d + 6, d + 6);
        }
    }

    // ── Click handling ───────────────────────────────────────────────────────────

    private void handleClick(int px, int py) {
        if (board == null || board.isGameOver()) return;

        int offset = CELL / 2;
        int col = (px - offset) / CELL;
        int row = (py - offset) / CELL;

        if (row < 0 || row >= board.getSize() || col < 0 || col >= board.getSize()) return;

        HoleState clicked = board.getHole(row, col);

        if (selectedRow == -1) {
            if (clicked == HoleState.PEG) {
                selectedRow = row;
                selectedCol = col;
                repaint();
            }
        } else {
            int fromRow = selectedRow, fromCol = selectedCol;
            selectedRow = -1;
            selectedCol = -1;

            if (clicked == HoleState.PEG && !(row == fromRow && col == fromCol)) {
                selectedRow = row;
                selectedCol = col;
                repaint();
            } else if (moveListener != null) {
                moveListener.onMoveAttempted(fromRow, fromCol, row, col);
            }
        }
    }
}