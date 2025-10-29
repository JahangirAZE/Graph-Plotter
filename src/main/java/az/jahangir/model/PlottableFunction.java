package az.jahangir.model;

import java.awt.Color;
import java.util.function.Function;

public record PlottableFunction(String name, Color color, Function<Double, Double> evaluator) {

    public Double evaluate(Double x) {
        return evaluator.apply(x);
    }
}