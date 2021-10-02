package io.github.ypankaj.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public enum PDNode {
    ID("id"),
    LABEL("label"),
    ENTITY_NAME("entityName"),
    UOI("unitsOfInformation"),
    STATE_VARIABLES("stateVariables"),
    MULTIMER("multimer"),
    CLONE_MARKER("cloneMarker"),
    CLONE_LABEL("cloneLabel");

    private String name;

    private PDNode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}