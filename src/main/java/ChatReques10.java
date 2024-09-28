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
public class ChatReques10 {
    private String model;
    private List<?> messages;
    private int n;
    private double temperature;
    private List<?> blablavariable;

    public boolean newMethod(String st1, String st2) {
        if (st1 == st2) {
            return true;
        }
        return false;
    }
    public String convertoToString(String str){
        return str;
    }
}
