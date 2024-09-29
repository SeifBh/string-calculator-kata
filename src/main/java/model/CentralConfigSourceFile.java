package model;

import java.util.Map;

public class CentralConfigSourceFile {

    private String name;
    private Map<String, String> source;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSource() {
        return source;
    }

    public void setSource(Map<String, String> source) {
        this.source = source;
    }
}
