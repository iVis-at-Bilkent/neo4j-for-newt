package io.github.ypankaj;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.neo4j.graphdb.Node;

public class PDLangNode {
    private String label;
	private String entityName;
	private String[] uoi;
    private String[] stateVariables;
    private Boolean multimer;
    private Boolean cloneMaker;
    private String cloneLabel;

    public PDLangNode(String label, String entityName, String[] uoi, String[] stateVariables, Boolean multimer, Boolean cloneMaker, String cloneLabel) {
        this.label = label;
        this.entityName = entityName;
        this.uoi = uoi;
        this.stateVariables = stateVariables;
        this.multimer = multimer;
        this.cloneMaker = cloneMaker;
        this.cloneLabel = cloneLabel;
    }

    public PDLangNode(Node node) {
        this.label = Helper.getNodeLabel(node);
        this.entityName = Helper.getNodeEntityName(node);
        this.uoi = Helper.getNodeUOI(node);
        this.stateVariables = Helper.getNodeStateVariables(node);
        this.multimer = Helper.getNodeMultimer(node);
        this.cloneMaker = Helper.getNodeCloneMarker(node);
        this.cloneLabel = Helper.getNodeCloneLabel(node);
    }

    public PDLangNode(Map<String, Object> row, String key) {
        Node node = (Node) row.get(key);
        this.label = Helper.getNodeLabel(node);
        this.entityName = Helper.getNodeEntityName(node);
        this.uoi = Helper.getNodeUOI(node);
        this.stateVariables = Helper.getNodeStateVariables(node);
        this.multimer = Helper.getNodeMultimer(node);
        this.cloneMaker = Helper.getNodeCloneMarker(node);
        this.cloneLabel = Helper.getNodeCloneLabel(node);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PDLangNode)) {
            return false;
        }
        PDLangNode pDLangNode = (PDLangNode) o;
        return label.equals(pDLangNode.label) && entityName.equals(pDLangNode.entityName) && Arrays.equals(uoi, pDLangNode.uoi) && Arrays.equals(stateVariables, pDLangNode.stateVariables) && (multimer == pDLangNode.multimer) && (cloneMaker == pDLangNode.cloneMaker) && cloneLabel.equals(pDLangNode.cloneLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, entityName, uoi, stateVariables, multimer, cloneMaker, cloneLabel);
    }

    @Override
    public String toString() {
        return "{" +
            " label='" + getLabel() + "'" +
            ", entityName='" + getEntityName() + "'" +
            ", uoi=[" + String.join(", ", getUoi()) + "]" +
            ", stateVariables=[" + String.join(", ", getStateVariables()) + "]" +
            ", multimer='" + isMultimer() + "'" +
            ", cloneMaker='" + isCloneMaker() + "'" +
            ", cloneLabel='" + getCloneLabel() + "'" +
            "}";
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String[] getUoi() {
        return this.uoi;
    }

    public void setUoi(String[] uoi) {
        this.uoi = uoi;
    }

    public String[] getStateVariables() {
        return this.stateVariables;
    }

    public void setStateVariables(String[] stateVariables) {
        this.stateVariables = stateVariables;
    }

    public Boolean isMultimer() {
        return this.multimer;
    }

    public Boolean getMultimer() {
        return this.multimer;
    }

    public void setMultimer(Boolean multimer) {
        this.multimer = multimer;
    }

    public Boolean isCloneMaker() {
        return this.cloneMaker;
    }

    public Boolean getCloneMaker() {
        return this.cloneMaker;
    }

    public void setCloneMaker(Boolean cloneMaker) {
        this.cloneMaker = cloneMaker;
    }

    public String getCloneLabel() {
        return this.cloneLabel;
    }

    public void setCloneLabel(String cloneLabel) {
        this.cloneLabel = cloneLabel;
    }

}
