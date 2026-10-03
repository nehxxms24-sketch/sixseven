package ui;

import dao.CategoryDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Date;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

/**
 * TrendsChartPanel – custom AWT/Java2D bar chart (no external libraries).
 *
 * Renders a colour-coded, anti-aliased vertical bar chart with:
 *   • Labelled X-axis (category names, angled)
 *   • Labelled Y-axis (₹ values, auto-scaled)
 *   • Horizontal grid lines
 *   • Rounded gradient bars
 *   • ₹ value labels above each bar
 *   • Legend panel on the right
 *   • "Total Spend" summary label
 *
 * Call {@link #refreshData(Date, Date)} to reload from the DB.
 */
public class TrendsChartPanel extends JPanel {

    // ── Layout constants ───────────────────────────────────────────────────
    private static final int PADDING_LEFT   = 80;
    private static final int PADDING_RIGHT  = 220;   // room for legend
    private static final int PADDING_TOP    = 50;
    private static final int PADDING_BOTTOM = 90;

    private static final int BAR_GAP_RATIO  = 3;     // bar width = slot / BAR_GAP_RATIO

    // ── Colour palette (one per bar / legend item) ─────────────────────────
    private static final Color[] PALETTE = {
        new Color(0xFF6B6B),  // coral-red
        new Color(0x4ECDC4),  // teal
        new Color(0xFFE66D),  // amber
        new Color(0x6C5CE7),  // violet
        new Color(0xA8E063),  // lime
        new Color(0xFD79A8),  // pink
        new Color(0x74B9FF),  // sky-blue
        new Color(0x55EFC4),  // mint
        new Color(0xFDCB6E),  // peach
        new Color(0xE17055),  // burnt orange
        new Color(0x00B894),  // emerald
        new Color(0xD63031),  // crimson
    };

    // ── Dark theme colours ─────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(0x1A1A2E);
    private static final Color CARD_BG      = new Color(0x16213E);
    private static final Color AXIS_COLOR   = new Color(0xE0E0E0);
    private static final Color GRID_COLOR   = new Color(0x2A2A4A);
    private static final Color TEXT_LIGHT   = new Color(0xF0F0F0);
    private static final Color TEXT_DIM     = new Color(0xA0A0C0);
    private static final Color ACCENT       = new Color(0x0F3460);

    // ── State ──────────────────────────────────────────────────────────────
    private List<String> categories = new ArrayList<>();
    private List<Double> amounts    = new ArrayList<>();
    private double       maxAmount  = 1.0;
    private double       totalSpend = 0.0;
    private String       statusMsg  = "Loading data…";

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final NumberFormat rupeeFormat;

    // ──────────────────────────────────────────────────────────────────────
    public TrendsChartPanel() {
        setBackground(BG_DARK);
        setPreferredSize(new Dimension(800, 450));

        rupeeFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        rupeeFormat.setMaximumFractionDigits(0);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Public API
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Reloads category-wise totals from the DB and repaints the chart.
     * Safe to call from the Event Dispatch Thread; executes DB work on a
     * background SwingWorker so the UI stays responsive.
     *
     * @param startDate inclusive lower bound (null = no bound)
     * @param endDate   inclusive upper bound (null = no bound)
     */
    public void refreshData(Date startDate, Date endDate) {
        statusMsg = "Loading…";
        repaint();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Object[]> rows = categoryDAO.getCategoryTotals(startDate, endDate);

                List<String> cats  = new ArrayList<>();
                List<Double> amts  = new ArrayList<>();
                double max   = 0.0;
                double total = 0.0;

                for (Object[] row : rows) {
                    cats.add((String) row[0]);
                    double v = (Double) row[1];
                    amts.add(v);
                    if (v > max) max = v;
                    total += v;
                }

                categories = cats;
                amounts    = amts;
                maxAmount  = max > 0 ? max : 1.0;
                totalSpend = total;
                statusMsg  = null;
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // surface any exception
                } catch (Exception ex) {
                    statusMsg = "Error: " + ex.getCause().getMessage();
                }
                repaint();
            }
        };
        worker.execute();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Core painting
    // ══════════════════════════════════════════════════════════════════════

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // ── Anti-aliasing ──────────────────────────────────────────────
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);

            int W = getWidth();
            int H = getHeight();

            drawBackground(g2, W, H);

            if (statusMsg != null) {
                drawCentredMessage(g2, W, H, statusMsg);
                return;
            }

            if (categories.isEmpty()) {
                drawCentredMessage(g2, W, H, "No expense data for selected period.");
                return;
            }

            drawTitle(g2, W);
            drawAxes(g2, W, H);
            drawGridLines(g2, W, H);
            drawBars(g2, W, H);
            drawLegend(g2, W, H);
            drawTotalLabel(g2, W, H);

        } finally {
            g2.dispose();
        }
    }

    // ── Sub-drawing methods ────────────────────────────────────────────────

    private void drawBackground(Graphics2D g2, int W, int H) {
        // gradient background
        GradientPaint gp = new GradientPaint(0, 0, BG_DARK, W, H, ACCENT);
        g2.setPaint(gp);
        g2.fillRect(0, 0, W, H);

        // chart card
        g2.setColor(new Color(0x0D1B2A));
        RoundRectangle2D card = new RoundRectangle2D.Float(10, 10, W - 20, H - 20, 20, 20);
        g2.fill(card);
        g2.setColor(new Color(0x1E3A5F));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(card);
    }

    private void drawTitle(Graphics2D g2, int W) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(TEXT_LIGHT);
        String title = "Expense by Category";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (W - PADDING_RIGHT - PADDING_LEFT - fm.stringWidth(title)) / 2 + PADDING_LEFT;
        g2.drawString(title, tx, 35);
    }

    private int chartTop()    { return PADDING_TOP; }
    private int chartBottom() { return getHeight() - PADDING_BOTTOM; }
    private int chartLeft()   { return PADDING_LEFT; }
    private int chartRight()  { return getWidth() - PADDING_RIGHT; }

    private void drawAxes(Graphics2D g2, int W, int H) {
        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(1.5f));

        // Y-axis
        g2.drawLine(chartLeft(), chartTop(), chartLeft(), chartBottom());
        // X-axis
        g2.drawLine(chartLeft(), chartBottom(), chartRight(), chartBottom());

        // Y-axis label
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(TEXT_DIM);
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.translate(15, (chartTop() + chartBottom()) / 2);
        g2r.rotate(-Math.PI / 2);
        g2r.drawString("Amount (₹)", -35, 0);
        g2r.dispose();

        // Y-axis tick values (5 ticks)
        int ticks = 5;
        for (int i = 0; i <= ticks; i++) {
            double val = (maxAmount / ticks) * i;
            int    y   = chartBottom() - (int)((val / maxAmount) * chartHeight());

            g2.setColor(AXIS_COLOR);
            g2.drawLine(chartLeft() - 4, y, chartLeft(), y);

            g2.setColor(TEXT_DIM);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String label = rupeeFormat.format(val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, chartLeft() - fm.stringWidth(label) - 6, y + 4);
        }
    }

    private void drawGridLines(Graphics2D g2, int W, int H) {
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));

        int ticks = 5;
        for (int i = 1; i <= ticks; i++) {
            double val = (maxAmount / ticks) * i;
            int    y   = chartBottom() - (int)((val / maxAmount) * chartHeight());
            g2.drawLine(chartLeft() + 1, y, chartRight(), y);
        }
    }

    private void drawBars(Graphics2D g2, int W, int H) {
        int n      = categories.size();
        int cWidth = (chartRight() - chartLeft()) / n;
        int barW   = Math.max(6, cWidth / BAR_GAP_RATIO);

        for (int i = 0; i < n; i++) {
            double amt  = amounts.get(i);
            int    barH = (int)((amt / maxAmount) * chartHeight());
            int    x    = chartLeft() + i * cWidth + (cWidth - barW) / 2;
            int    y    = chartBottom() - barH;

            Color base = PALETTE[i % PALETTE.length];
            Color top  = base.brighter();

            // gradient bar
            GradientPaint gp = new GradientPaint(x, y, top, x, chartBottom(), base.darker());
            g2.setPaint(gp);
            RoundRectangle2D bar = new RoundRectangle2D.Float(x, y, barW, barH, 8, 8);
            g2.fill(bar);

            // bar border
            g2.setColor(base.brighter().brighter());
            g2.setStroke(new BasicStroke(1f));
            g2.draw(bar);

            // ₹ value above bar
            g2.setColor(TEXT_LIGHT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            String valStr = rupeeFormat.format(amt);
            FontMetrics fm = g2.getFontMetrics();
            int labelX = x + (barW - fm.stringWidth(valStr)) / 2;
            if (labelX < chartLeft()) labelX = chartLeft() + 2;
            g2.drawString(valStr, labelX, y - 4);

            // X-axis category label (angled)
            g2.setColor(TEXT_DIM);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            Graphics2D g2r = (Graphics2D) g2.create();
            int cx = x + barW / 2;
            g2r.translate(cx, chartBottom() + 8);
            g2r.rotate(Math.PI / 4);
            String catLabel = shorten(categories.get(i), 14);
            g2r.setColor(TEXT_DIM);
            g2r.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2r.drawString(catLabel, 0, 0);
            g2r.dispose();
        }
    }

    private void drawLegend(Graphics2D g2, int W, int H) {
        int lx   = chartRight() + 15;
        int ly   = chartTop() + 10;
        int swatch = 12;
        int lineH  = 22;

        g2.setColor(new Color(0x0D1B2A));
        int boxH = categories.size() * lineH + 20;
        g2.fillRoundRect(lx - 8, ly - 8, PADDING_RIGHT - 20, boxH, 10, 10);
        g2.setColor(new Color(0x1E3A5F));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx - 8, ly - 8, PADDING_RIGHT - 20, boxH, 10, 10);

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(TEXT_LIGHT);
        g2.drawString("Legend", lx, ly + 6);
        ly += 18;

        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i < categories.size(); i++) {
            Color c = PALETTE[i % PALETTE.length];
            g2.setColor(c);
            g2.fillRoundRect(lx, ly, swatch, swatch, 4, 4);
            g2.setColor(TEXT_LIGHT);
            String label = shorten(categories.get(i), 16);
            g2.drawString(label, lx + swatch + 5, ly + swatch - 2);
            ly += lineH;
        }
    }

    private void drawTotalLabel(Graphics2D g2, int W, int H) {
        String txt = "Total Spend: " + rupeeFormat.format(totalSpend);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(0xFFE66D));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, chartLeft(), H - 12);
    }

    private void drawCentredMessage(Graphics2D g2, int W, int H, String msg) {
        g2.setFont(new Font("SansSerif", Font.ITALIC, 15));
        g2.setColor(TEXT_DIM);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg,
                (W - fm.stringWidth(msg)) / 2,
                (H + fm.getAscent()) / 2);
    }

    // ── Utilities ──────────────────────────────────────────────────────────

    private int chartHeight() {
        return chartBottom() - chartTop();
    }

    private String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
