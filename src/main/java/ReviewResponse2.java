public class ReviewResponse2 {
    private int line;
    private String comment;

    public ReviewResponse2(int line, String comment) {
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
