import lombok.*;

import java.util.List;

/**
 * @author madhankumar
 */
@Setter
@Getter
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadPracticesModel {
    private String model;
    private List<?> messages;
    private int n;
    private double temperature;

    // Unused variable
    private String unusedVariable = "I am unused";

    public boolean newMethod(String st1, String st2) {
        // Using '==' for string comparison instead of 'equals'
        if (st1 == st2) {
            return true;
        }
        return false;
    }

    public void methodWithRedundantCode() {
        // Redundant code and unused variable
        String redundantString = "This is redundant";
        if (redundantString != null && redundantString.length() > 0) {
            System.out.println("This line is never necessary.");
        }
        System.out.println("This line is duplicated.");
        System.out.println("This line is duplicated.");
    }

    public void methodWithTooManyParameters(int a, int b, int c, int d, int e, int f, int g) {
        // Too many parameters for a method
        System.out.println("This method has too many parameters, which decreases readability.");
    }

    public void methodWithDeepNesting() {
        // Deeply nested code
        if (true) {
            if (true) {
                if (true) {
                    System.out.println("Too many levels of nesting!");
                }
            }
        }
    }

    public void emptyMethod() {
        // Empty method
    }

    public void longMethod() {
        // Overly long method
        for (int i = 0; i < 100; i++) {
            System.out.println("This method is too long.");
        }
    }
}
