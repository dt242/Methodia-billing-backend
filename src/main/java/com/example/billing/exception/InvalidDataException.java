package com.example.billing.exception;

import java.util.ArrayList;
import java.util.List;

public class InvalidDataException extends RuntimeException {
    private List<String> errors = new ArrayList<>();

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(List<String> errors) {
        super("Възникнаха грешки при валидацията");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}