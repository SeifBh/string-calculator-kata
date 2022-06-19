import java.util.Arrays;

public class StringCalculatorStep1 {
    public static final int Add(final String numbers) {
        int returnValue = 0;
        String[] numbersArray = numbers.split(",");
        if (numbersArray.length > 2) {
            throw new RuntimeException("Until 2 numbers seperated by commas is allowed");
        } else {
            if (!numbers.isEmpty()) {
                returnValue = Arrays.stream(numbersArray).mapToInt(Integer::parseInt).sum();
            }
        }
        return returnValue;
    }
}
