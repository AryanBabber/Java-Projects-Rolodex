package Calculator;

import java.util.Stack;

public class Calculate {
    public static double evaluate(String expression) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for ( int i = 0; i < expression.length(); i++ ) {
            char c = expression.charAt(i);

            if ( c == ' ' ) continue;

            if ( Character.isDigit(c) || c == '.' ) {
                StringBuilder sb = new StringBuilder();
                while ( i < expression.length() &&
                        ( Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.' ) ) {
                    sb.append(expression.charAt(i));
                    i++;
                }

                i--;
                values.push(Double.parseDouble(sb.toString()));
            } else if ( c == '(' ) {
                operators.push(c);
            } else if ( c == ')' ) {
                while ( !operators.isEmpty() && operators.peek() != '(' ) {
                    values.push(applyOperator(values.pop(), values.pop(), operators.pop()));
                }
                operators.pop();
            } else if ( isOperator(c) ) {
                while ( !operators.isEmpty() && precedence(c) <= precedence(operators.peek()) ) {
                    values.push(applyOperator(values.pop(), values.pop(), operators.pop()));
                }
                operators.push(c);
            }
        }

        while ( !operators.isEmpty() ) {
            values.push(applyOperator(values.pop(), values.pop(), operators.pop()));
        }

        return values.pop();
    }

    private static boolean isOperator(char op) {
        return op == '+' || op == '-' || op == '*' || op == '/' || op == '^';
    }

    private static int precedence(char op) {
        return switch ( op ) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '^' -> 3;
            default -> 0;
        };
    }

    private static double applyOperator(double b, double a, char op) {
        return switch ( op ) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if ( b == 0 ) throw new ArithmeticException("Cannot divide by 0!");
                yield a / b;
            }
            case '^' -> Math.pow(a, b);
            default -> throw new IllegalArgumentException("Invalid operator: " + op);
        };
    }
}
