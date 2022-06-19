import java.util.Arrays;

public class StringCalculatorStep2 {
    public static final int Add(final String numbers) {
        int returnValue = 0;
        String[] numbersArray = numbers.split(",");
            if (!numbers.isEmpty()) {
                returnValue = Arrays.stream(numbersArray).mapToInt(Integer::parseInt).sum();
            }
        return returnValue;
    }
}
