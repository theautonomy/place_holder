package com.example.demo.model;

import java.util.List;

public class ErrorGroup {

    private String groupId;
    private String groupName;
    private String representativeErrorType;
    private String representativeErrorMessage;
    private List<ErrorLog> errors;
    private int errorCount;
    private double avgSimilarity;
    private String severity;

    // Constructors
    public ErrorGroup() {}

    public ErrorGroup(
            String groupId,
            String groupName,
            String representativeErrorType,
            String representativeErrorMessage,
            List<ErrorLog> errors) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.representativeErrorType = representativeErrorType;
        this.representativeErrorMessage = representativeErrorMessage;
        this.errors = errors;
        this.errorCount = errors != null ? errors.size() : 0;
    }

    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRepresentativeErrorType() {
        return representativeErrorType;
    }

    public void setRepresentativeErrorType(String representativeErrorType) {
        this.representativeErrorType = representativeErrorType;
    }

    public String getRepresentativeErrorMessage() {
        return representativeErrorMessage;
    }

    public void setRepresentativeErrorMessage(String representativeErrorMessage) {
        this.representativeErrorMessage = representativeErrorMessage;
    }

    public List<ErrorLog> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorLog> errors) {
        this.errors = errors;
        this.errorCount = errors != null ? errors.size() : 0;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public double getAvgSimilarity() {
        return avgSimilarity;
    }

    public void setAvgSimilarity(double avgSimilarity) {
        this.avgSimilarity = avgSimilarity;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
