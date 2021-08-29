package io.github.ypankaj;

import java.util.Arrays;
import java.util.Objects;

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
