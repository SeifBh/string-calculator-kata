import org.junit.Assert;
import org.junit.Test;

public class StringCalculatorStep5Test {

    @Test(expected = RuntimeException.class)
    public final void whenPassedNegativeNumberThenRuntimeExceptionIsThrown() {
        StringCalculatorStep5.Add("3,-6,15,18,46,33");
    }
    @Test
    public final void whenPassedNegativeNumbersThenRuntimeExceptionIsThrown() {
        RuntimeException exception = null;
        try {
            StringCalculatorStep5.Add("3,-6,15,-18,46,33");
        } catch (RuntimeException e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals("Negatives not allowed: [-6, -18]", exception.getMessage());
    }
}
