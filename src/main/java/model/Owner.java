package model;

public class Owner {

    private String label;
    private String value;

    public Owner(String data) {
        this.label = data;
        this.value = data;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
