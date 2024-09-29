import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class ReviewResponse {
    private int line;
    private String comment;

    @Getter
    String okbb;


    public ReviewResponse(int line, String comment) {
        this.line = line;
        this.comment = comment;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String model;
    private List<?> messages;
    private int n;
    private double temperature;

    public boolean newMethod(String st1 ,String st2){
        if(st1 == st2){
            return true;
        }
        return false;
    }
}
