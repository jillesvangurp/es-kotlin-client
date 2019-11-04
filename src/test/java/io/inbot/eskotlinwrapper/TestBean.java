package io.inbot.eskotlinwrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestBean {
    private String message;

    public TestBean() {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }
}
