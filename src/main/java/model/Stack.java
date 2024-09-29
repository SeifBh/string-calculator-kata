package model;

import java.util.HashMap;
import java.util.Map;

public class Stack {

    private String name;
    private Map<String, Boolean> state;

    public Stack(String name) {
        this.state = new HashMap<>();
        this.name = name;
    }

    public void setState(String channel, boolean state) {
        this.state.put(channel, state);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getState() {
        return state;
    }

    public void setState(Map<String, Boolean> state) {
        this.state = state;
    }
}
