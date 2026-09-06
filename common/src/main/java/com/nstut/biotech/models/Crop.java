package com.nstut.biotech.models;

import java.util.List;

public record Crop(String seedId, List<Drop> yields) {

    public Crop(String seedId, List<Drop> yields) {
        this.seedId = seedId;
        this.yields = yields;
    }

    public String seedId() {
        return seedId;
    }

    public List<Drop> yields() {
        return yields;
    }
}
