package ru.eddyz.sellautorestapi.enums;

public enum DriveMode {
    AWD("Полный"), FRONT("Передний"), REAR("Задний");

    private final String mode;


    DriveMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return mode;
    }
}
