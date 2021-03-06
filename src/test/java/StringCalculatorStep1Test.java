import org.junit.Assert;
import org.junit.Test;

public class StringCalculatorStep1Test {

    @Test(expected = RuntimeException.class)
    public final void whenMoreThanTwoNumberThenRuntimeExceptionIsThrown() {
        StringCalculatorStep1.Add("1,2,3");
    }
    @Test
    public final void whenEmptyStringThenReturnZero() {
        Assert.assertEquals(0, StringCalculatorStep1.Add(""));
    }

    @Test
    public final void when2NumbersAreUsedThenNoExceptionIsThrown() {
        Assert.assertEquals(3, StringCalculatorStep1.Add("1,2"));
    }

    @Test(expected = RuntimeException.class)
    public final void whenNonNumberIsUsedThenExceptionIsThrown() {
        StringCalculatorStep1.Add("9,TEST");
    }

}
