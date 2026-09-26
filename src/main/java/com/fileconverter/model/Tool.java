package com.fileconverter.model;

public class Tool {
    private final int id;
    private final String name;

    public Tool(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}