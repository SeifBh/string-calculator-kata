import org.junit.Assert;
import org.junit.Test;

public class StringCalculatorStep2Test {

//    @Test(expected = RuntimeException.class)
//    public final void whenMoreThanTwoNumberThenRuntimeExceptionIsThrown() {
//        StringCalculatorStep1.Add("1,2,3");
//    }

    @Test
    public final void whenUnknownAmountOfNumbersRetrunSumOfValues() {
        Assert.assertEquals(5+7+2+18+6, StringCalculatorStep2.Add("5,7,2,18,6"));
    }

}
