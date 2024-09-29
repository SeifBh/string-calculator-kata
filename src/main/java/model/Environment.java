package model;

import java.util.ArrayList;
import java.util.List;
public class Environment {

    private String name;
    private List<Stack> stacksState;

    public Environment(String name) {
        this.name = name;
        this.stacksState = new ArrayList<>();
    }

    public void addStack(Stack stack) {
        this.stacksState.add(stack);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Stack> getStacksState() {
        return stacksState;
    }

    public void setStacksState(List<Stack> stacksState) {
        this.stacksState = stacksState;
    }
}
