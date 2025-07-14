package az.jahangir.model;

import java.awt.*;
import java.util.function.Function;

public class PlottableFunction {

    private final String name;
    private final Color color;
    private final Function<Double, Double> evaluator;

    public PlottableFunction(String name, Color color, Function<Double, Double> evaluator) {
        this.name = name;
        this.color = color;
        this.evaluator = evaluator;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public Double evaluate(Double x) {
        return evaluator.apply(x);
    }
}