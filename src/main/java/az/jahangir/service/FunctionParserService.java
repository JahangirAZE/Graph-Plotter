package az.jahangir.service;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.Objects;

public class FunctionParserService {

    public static Expression parse(String functionString) throws IllegalArgumentException {
        if (Objects.isNull(functionString) || functionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Function string cannot be null or empty");
        }
        return new ExpressionBuilder(functionString)
                .variable("x")
                .build();
    }
}