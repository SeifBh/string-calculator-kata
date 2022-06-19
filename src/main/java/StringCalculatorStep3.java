import java.util.Arrays;

public class StringCalculatorStep3 {
    public static final int Add(final String numbers) {
        int returnValue = 0;
        String[] numbersArray = numbers.split(",|\n");
        if (numbers.toLowerCase().contains(",\n") || numbers.toLowerCase().contains("\n,") ) {
            throw new RuntimeException("Incorrect Syntax");
        } else {
            if (!numbers.isEmpty()) {
                returnValue = Arrays.stream(numbersArray).mapToInt(Integer::parseInt).sum();
            }
        }
        return returnValue;
    }
}
