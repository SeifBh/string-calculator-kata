import java.util.Arrays;

public class StringCalculatorStep4 {
    public static final int Add(final String numbers) {
        String delimiter = ",|\n";
        String numbersWithoutDelimiter = numbers;
        boolean newLine = true;

        if (numbers.startsWith("//")) {
            int delimiterIndex = numbers.indexOf("//") + 2;
            delimiter = numbers.substring(delimiterIndex, delimiterIndex + 1);
            if(numbers.indexOf("\n") == 3){
                numbersWithoutDelimiter = numbers.substring(numbers.indexOf("\n") + 1);
            }
            else{
                numbersWithoutDelimiter = numbers.substring(numbers.indexOf(delimiter) + 1);
            }
        }
        return Add(numbersWithoutDelimiter, delimiter);
    }

    public static final int Add(final String numbers, final String delimeters) {
        int returnValue = 0;
        String[] numbersArray = numbers.split(delimeters);
        if (!numbers.isEmpty()) {
            returnValue = Arrays.stream(numbersArray).mapToInt(Integer::parseInt).sum();
        }
        return returnValue;
    }
}
