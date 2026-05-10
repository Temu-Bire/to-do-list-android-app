package com.example.to_dolist.domain.model;

public class Category {
    private final int id;
    private final String name;
    private final String colorHex; // e.g. "#FF5252"

    public Category(int id, String name, String colorHex) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getColorHex() { return colorHex; }
}
