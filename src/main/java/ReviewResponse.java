import lombok.Data;
import lombok.Getter;
import lombok.ToString;

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
}
