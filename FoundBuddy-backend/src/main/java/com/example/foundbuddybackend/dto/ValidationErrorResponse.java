package com.example.foundbuddybackend.dto;

import java.util.List;
import java.util.Map;

public class ValidationErrorResponse {
    
    private String message;
    private Map<String, List<String>> errors;

    public ValidationErrorResponse() {}

    public ValidationErrorResponse(String message, Map<String, List<String>> errors) {
        this.message = message;
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }
}
