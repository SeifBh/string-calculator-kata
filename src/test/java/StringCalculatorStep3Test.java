import org.junit.Assert;
import org.junit.Test;

public class StringCalculatorStep3Test {

    @Test
    public final void when2NumbersAreUsedThenNoExceptionIsThrown() {
        Assert.assertEquals(3, StringCalculatorStep3.Add("1,2"));
    }

    @Test
    public final void when2NumbersAreUsedWithSecondDelimeterThenNoExceptionIsThrown() {
        Assert.assertEquals(6, StringCalculatorStep3.Add("1\n2,3"));
    }

    @Test(expected = RuntimeException.class)
    public final void whenInputsSyntaxIsNotCorrect() {
       StringCalculatorStep3.Add("1,\n");
    }

}
