package az.jahangir.view;

import az.jahangir.model.GraphModel;
import az.jahangir.model.PlottableFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.List;

public class GraphPanel extends JPanel implements PropertyChangeListener {

    private final GraphModel model;
    private final int PADDING = 25;

    public GraphPanel(GraphModel model) {
        this.model = model;
        this.model.addPropertyChangeListener(this);
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) { repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        double xMin = model.getXMin();
        double xMax = model.getXMax();
        double yMin = model.getYMin();
        double yMax = model.getYMax();

        double xScale = (double) (width - 2 * PADDING) / (xMax - xMin);
        double yScale = (double) (height - 2 * PADDING) / (yMax - yMin);
        int originX = PADDING - (int) (xMin * xScale);
        int originY = height - PADDING + (int) (yMin * yScale);

        drawGrid(g2d, width, height, originX, originY, xScale, yScale);
        drawAxes(g2d, width, height, originX, originY);

        for (PlottableFunction function : model.getFunctions()) {
            drawSingleFunction(g2d, width, height, xScale, yScale, function);
        }

        drawTrackedPoints(g2d, width, height, xScale, yScale);
        drawLegend(g2d, width, height);
    }

    private void drawSingleFunction(Graphics2D g2d, int width, int height, double xScale, double yScale, PlottableFunction function) {
        g2d.setColor(function.getColor());
        g2d.setStroke(new BasicStroke(2f));
        Path2D.Double path = new Path2D.Double();
        boolean firstPoint = true;

        for (int px = PADDING; px <= width - PADDING; px++) {
            double x = model.getXMin() + (px - PADDING) / xScale;
            try {
                double y = function.evaluate(x);

                if (Double.isFinite(y) && y >= model.getYMin() && y <= model.getYMax()) {
                    int py = height - PADDING - (int) ((y - model.getYMin()) * yScale);
                    if (firstPoint) {
                        path.moveTo(px, py);
                        firstPoint = false;
                    } else {
                        path.lineTo(px, py);
                    }
                } else {
                    firstPoint = true;
                }
            } catch (Exception ex) {
                firstPoint = true;
            }
        }
        g2d.draw(path);
    }

    private void drawLegend(Graphics2D g2d, int width, int height) {
        if (model.getFunctions().isEmpty()) return;

        Font legendFont = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(legendFont);
        FontMetrics fm = g2d.getFontMetrics();
        int legendPadding = 10;
        int lineSpacing = 5;

        int maxTextWidth = 0;
        for (PlottableFunction func : model.getFunctions()) {
            int textWidth = fm.stringWidth(func.getName());
            if (textWidth > maxTextWidth) maxTextWidth = textWidth;
        }

        int boxWidth = maxTextWidth + (2 * legendPadding);
        int boxHeight = (model.getFunctions().size() * fm.getHeight()) + ((model.getFunctions().size() - 1) * lineSpacing) + (2 * legendPadding);

        int boxX = width - PADDING - boxWidth - legendPadding;
        int boxY = height - PADDING - boxHeight - legendPadding;

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillRect(boxX, boxY, boxWidth, boxHeight);

        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(boxX, boxY, boxWidth, boxHeight);

        int textX = boxX + legendPadding;
        int currentY = boxY + legendPadding + fm.getAscent();

        for (PlottableFunction func : model.getFunctions()) {
            g2d.setColor(func.getColor());
            g2d.drawString(func.getName(), textX, currentY);
            currentY += fm.getHeight() + lineSpacing;
        }
    }

    private void drawTrackedPoints(Graphics2D g2d, int width, int height, double xScale, double yScale) {
        if (!model.isTracking()) return;

        List<PlottableFunction> functions = model.getFunctions();
        List<Point2D.Double> points = model.getTrackedPoints();

        for (int i = 0; i < functions.size() && i < points.size(); i++) {
            Point2D.Double point = points.get(i);
            PlottableFunction func = functions.get(i);

            if (point == null) continue;

            double mathX = point.getX();
            double mathY = point.getY();

            if (mathX < model.getXMin() || mathX > model.getXMax() || mathY < model.getYMin() || mathY > model.getYMax()) continue;

            int pixelX = PADDING + (int) ((mathX - model.getXMin()) * xScale);
            int pixelY = height - PADDING - (int) ((mathY - model.getYMin()) * yScale);

            g2d.setColor(func.getColor());
            g2d.fillOval(pixelX - 5, pixelY - 5, 10, 10);

            if (i == 0) {
                g2d.setColor(new Color(150, 150, 150, 150));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawLine(pixelX, PADDING, pixelX, height - PADDING);
                g2d.drawLine(PADDING, pixelY, width - PADDING, pixelY);
            }
        }
    }

    private void drawAxes(Graphics2D g2d, int width, int height, int originX, int originY) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));

        g2d.drawLine(PADDING, originY, width - PADDING, originY);
        g2d.drawLine(originX, PADDING, originX, height - PADDING);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("X", width - PADDING, originY - 5);
        g2d.drawString("Y", originX + 5, PADDING);
    }

    private void drawGrid(Graphics2D g2d, int width, int height, int originX, int originY, double xScale, double yScale) {
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1f));
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        double xMin = model.getXMin();
        double xMax = model.getXMax();
        double yMin = model.getYMin();
        double yMax = model.getYMax();

        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        double xStep = Math.pow(10, Math.floor(Math.log10(xRange)) - 1);
        double yStep = Math.pow(10, Math.floor(Math.log10(yRange)) - 1);

        if (xRange / xStep > 20) xStep *= 5;
        else if (xRange / xStep > 10) xStep *= 2;
        if (yRange / yStep > 20) yStep *= 5;
        else if (yRange / yStep > 10) yStep *= 2;

        for (double x = Math.floor(xMin / xStep) * xStep; x <= xMax; x += xStep) {
            if (Math.abs(x) < 1E-9 * xStep) continue;
            int px = PADDING + (int) ((x - xMin) * xScale);
            g2d.drawLine(px, PADDING, px, height - PADDING);
            g2d.drawString(String.format("%.2g", x), px + 2, originY - 2);
        }

        for (double y = Math.floor(yMin / yStep) * yStep; y <= yMax; y += yStep) {
            if (Math.abs(y) < 1E-9 * yStep) continue;
            int py = height - PADDING - (int) ((y - yMin) * yScale);
            g2d.drawLine(PADDING, py, width - PADDING, py);
            g2d.drawString(String.format("%.2g", y), originX + 2, py - 2);
        }
    }
}