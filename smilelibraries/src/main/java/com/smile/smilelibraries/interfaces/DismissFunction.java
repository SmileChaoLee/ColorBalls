package com.smile.smilelibraries.interfaces;

public interface DismissFunction {
    void backgroundWork();
    void executeDismiss();
    void afterFinished(boolean isAdShown);
}
