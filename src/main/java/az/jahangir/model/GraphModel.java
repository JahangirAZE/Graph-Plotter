package az.jahangir.model;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GraphModel {

    public static final String TRACKING_PROPERTY = "tracking";
    public static final String VIEWPORT_PROPERTY = "viewport";
    public static final String FUNCTIONS_PROPERTY = "functions";

    private String rawUserInputFunction;
    private List<PlottableFunction> functions = new ArrayList<>();

    private static final double DEFAULT_MIN_X = -10;
    private static final double DEFAULT_MAX_X = 10;
    private static final double DEFAULT_MIN_Y = -10;
    private static final double DEFAULT_MAX_Y = 10;

    private double xMin, xMax, yMin, yMax;
    private final PropertyChangeSupport support;

    private List<Point2D.Double> trackedPoints = new ArrayList<>();

    public GraphModel() {
        this.support = new PropertyChangeSupport(this);
        reset();
    }

    public void reset() {
        setViewport(DEFAULT_MIN_X, DEFAULT_MAX_X, DEFAULT_MIN_Y, DEFAULT_MAX_Y);
    }

    public double getXMin() {
        return xMin;
    }

    public double getXMax() {
        return xMax;
    }

    public double getYMin() {
        return yMin;
    }

    public double getYMax() {
        return yMax;
    }

    public String getRawUserInputFunction() {
        return rawUserInputFunction;
    }

    public List<Point2D.Double> getTrackedPoints() {
        return Collections.unmodifiableList(trackedPoints);
    }

    public boolean isTracking() {
        return !trackedPoints.isEmpty();
    }

    public List<PlottableFunction> getFunctions() {
        return Collections.unmodifiableList(functions);
    }

    public void setFunctions(Collection<PlottableFunction> functions, String rawUserInputFunction) {
        this.rawUserInputFunction = rawUserInputFunction;
        var oldFunctions = this.functions;
        this.functions = new ArrayList<>(functions);
        support.firePropertyChange(FUNCTIONS_PROPERTY, oldFunctions, this.functions);
    }

    public void setTrackedPoints(List<Point2D.Double> points) {
        support.firePropertyChange(TRACKING_PROPERTY, null, null);
        this.trackedPoints = new ArrayList<>(points);
    }

    public void clearTrackedPoints() {
        if (!trackedPoints.isEmpty()) {
            setTrackedPoints(Collections.emptyList());
        }
    }

    public void clearFunctions() {
        setFunctions(Collections.emptyList(), "");
    }

    public void setViewport(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        support.firePropertyChange(VIEWPORT_PROPERTY, null, null);
    }

    public void pan(double deltaX, double deltaY) {
        setViewport(xMin + deltaX, xMax + deltaX, yMin + deltaY, yMax + deltaY);
    }

    public void zoom(double factor, double centerX, double centerY) {
        double newXMin = centerX - (centerX - xMin) / factor;
        double newXMax = centerX + (xMax - centerX) / factor;
        double newYMin = centerY - (centerY - yMin) / factor;
        double newYMax = centerY + (yMax - centerY) / factor;
        setViewport(newXMin, newXMax, newYMin, newYMax);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }
}