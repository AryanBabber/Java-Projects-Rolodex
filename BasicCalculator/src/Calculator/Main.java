package Calculator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Expression: ");
        String expression = sc.nextLine();

        double ans = Calculate.evaluate(expression);
        System.out.println(ans);
    }
}
