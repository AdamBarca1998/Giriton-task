package com.example.application.shared.enums;

/**
 * @author : Adam Barča
 * @created : 6. 6. 2022
 **/
public enum ETaskState {

    RUN("Běží"),
    WAIT("Zařazena do fronty"),
    FINISH("Skončila"),
    ERROR("Selhala")
    ;

    private String czText;

    ETaskState(String czText) {
        this.czText = czText;
    }

    public String getCzText() {
        return czText;
    }
}
