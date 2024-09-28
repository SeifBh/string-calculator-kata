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
public class ChatReques6 {
    private String model;
    private List<?> messages;
    private int n;
    private double temperature;

    public boolean newMethod(String st1, String st2) {
        if (st1 == st2) {
            return true;
        }
        return false;
    }
}
