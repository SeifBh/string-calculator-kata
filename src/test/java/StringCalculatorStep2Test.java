import org.junit.Assert;
import org.junit.Test;

public class StringCalculatorStep2Test {

    @Test
    public final void whenUnknownAmountOfNumbersRetrunSumOfValues() {
        Assert.assertEquals(5+7+2+18+6, StringCalculatorStep2.Add("5,7,2,18,6"));
    }

}
