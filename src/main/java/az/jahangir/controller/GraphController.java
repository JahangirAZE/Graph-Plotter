package az.jahangir.controller;

import az.jahangir.model.GraphModel;
import az.jahangir.model.PlottableFunction;
import az.jahangir.service.FunctionParserService;
import az.jahangir.view.MainFrame;
import net.objecthunter.exp4j.Expression;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.List;

public class GraphController {

    private static final double ZOOM_FACTOR = 1.1;
    private static final double H = 1e-7;

    private final GraphModel model;
    private final MainFrame view;
    private Point lastPanPoint;

    private static final Pattern SHORTHAND_POWER_PATTERN = Pattern.compile("\\(\\s*([a-zA-Z]+)\\s*\\^\\s*([0-9.]+)\\s*\\)");

    public GraphController(GraphModel model, MainFrame view) {
        this.model = model;
        this.view = view;
    }

    public void initController() {
        view.getPlotButton().addActionListener(e -> plotFunction());
        view.getFunctionField().addActionListener(e -> plotFunction());
        view.getResetButton().addActionListener(e -> model.reset() );
        view.getDerivativeCheckBox().addActionListener(e -> plotFunction());

        GraphPanelMouseAdapter mouseAdapter = new GraphPanelMouseAdapter();
        view.getGraphPanel().addMouseMotionListener(mouseAdapter);
        view.getGraphPanel().addMouseListener(mouseAdapter);
        view.getGraphPanel().addMouseWheelListener(this::handleMouseWheelZoom);

        PanDragListener panListener = new PanDragListener();
        view.getGraphPanel().addMouseListener(panListener);
        view.getGraphPanel().addMouseMotionListener(panListener);

        plotFunction();
    }

    private String preprocessFunctionString(String functionString) {
        return SHORTHAND_POWER_PATTERN.matcher(functionString).replaceAll("($1$3)^$2");
    }

    private void plotFunction() {
        model.clearTrackedPoints();
        String rawFunctionString = view.getFunctionField().getText();
        String processedFuncStr = preprocessFunctionString(rawFunctionString);

        List<PlottableFunction> functionsToPlot = new ArrayList<>();

        try {
            Expression f_x = FunctionParserService.parse(processedFuncStr);

            functionsToPlot.add(new PlottableFunction(
                    "f(x)",
                    Color.BLUE,
                    (x) -> {
                        f_x.setVariable("x", x);
                        return f_x.evaluate();
                    }
            ));

            if (view.getDerivativeCheckBox().isSelected()) {
                functionsToPlot.add(new PlottableFunction(
                        "f'(x)",
                        Color.RED,
                        (x) -> {
                            f_x.setVariable("x", x + H);
                            double y1 = f_x.evaluate();
                            f_x.setVariable("x", x - H);
                            double y2 = f_x.evaluate();
                            return (y1 - y2) / (2 * H);
                        }
                ));
            }

            model.setFunctions(functionsToPlot, rawFunctionString);

            view.getStatusLabel().setText("Plot Successful. Hover for f(x) coordinates.");
            view.getStatusLabel().setForeground(new Color(0, 128, 0));
            view.getFunctionField().setBackground(Color.WHITE);

        } catch (IllegalArgumentException ex) {
            model.clearFunctions();
            view.getStatusLabel().setText("Error: " + ex.getMessage());
            view.getStatusLabel().setForeground(Color.RED);
            view.getFunctionField().setBackground(new Color(255, 210, 210));
        }
    }

    private void handleMouseWheelZoom(MouseWheelEvent e) {
        double zoomDirection = e.getWheelRotation() < 0 ? ZOOM_FACTOR : 1 / ZOOM_FACTOR;
        Point cursor = e.getPoint();

        double xScale = (double) (view.getGraphPanel().getWidth()) / (model.getXMax() - model.getXMin());
        double yScale = (double) (view.getGraphPanel().getHeight()) / (model.getYMax() - model.getYMin());

        double cursorX = model.getXMin() + cursor.x / xScale;
        double cursorY = model.getYMin() - cursor.y / yScale;

        model.zoom(zoomDirection, cursorX, cursorY);
    }

    private class PanDragListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            lastPanPoint = e.getPoint();
            view.getGraphPanel().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            lastPanPoint = null;
            view.getGraphPanel().setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (Objects.isNull(lastPanPoint)) return;

            Point currentPoint = e.getPoint();

            double dx = (double) (currentPoint.x - lastPanPoint.x) / view.getGraphPanel().getWidth() * (model.getXMax() - model.getXMin());
            double dy = (double) (currentPoint.y - lastPanPoint.y) / view.getGraphPanel().getHeight() * (model.getYMax() - model.getYMin());

            model.pan(-dx, dy);
            lastPanPoint = currentPoint;
        }
    }

    private class GraphPanelMouseAdapter extends MouseAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            if (model.getFunctions().isEmpty()) return;

            int pixelX = e.getX();
            int padding = 25;
            int graphWidth = view.getGraphPanel().getWidth() - (2 * padding);
            double mathX = model.getXMin() + (pixelX - (double) padding) / graphWidth * (model.getXMax() - model.getXMin());

            List<Point2D.Double> newPoints = new ArrayList<>();
            StringBuilder statusText = new StringBuilder();
            statusText.append(String.format("x = %.3f", mathX));

            for (PlottableFunction func : model.getFunctions()) {
                try {
                    double mathY = func.evaluate(mathX);
                    newPoints.add(new Point2D.Double(mathX, mathY));
                    statusText.append(String.format(" | %s: %.3f", func.name(), mathY));
                } catch (Exception ex) {
                    newPoints.add(null);
                }
            }

            model.setTrackedPoints(newPoints);
            view.getStatusLabel().setText(statusText.toString());
            view.getStatusLabel().setForeground(Color.BLUE);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            model.clearTrackedPoints();
            if (!model.getRawUserInputFunction().isEmpty()) {
                view.getStatusLabel().setText("Plot Successful. Hover for f(x) coordinates.");
                view.getStatusLabel().setForeground(new Color(0, 128, 0));
            } else {
                view.getStatusLabel().setText("Enter a valid function");
                view.getStatusLabel().setForeground(Color.BLACK);
            }
        }
    }
}