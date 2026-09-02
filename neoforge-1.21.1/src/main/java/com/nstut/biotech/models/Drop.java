package com.nstut.biotech.models;

public record Drop(String id, int count, float chance) {

    public Drop(String id, int count) {
        this(id, count, 1.0f);
    }

    public String id() {
        return id;
    }

    public int count() {
        return count;
    }

    public float chance() {
        return chance;
    }
}
