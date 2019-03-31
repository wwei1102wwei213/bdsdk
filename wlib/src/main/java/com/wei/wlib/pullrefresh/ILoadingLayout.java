package com.wei.wlib.pullrefresh;

public interface ILoadingLayout {

    public enum State {
        NONE,
        RESET,
        PULL_TO_REFRESH,
        RELEASE_TO_REFRESH,
        REFRESHING,
        LOADING,
        NO_MORE_DATA,
        NO_NETWORK,
        NO_DATA
    }

    int getContentSize();

    State getState();

    void onPull(float f);

    void setState(State state);
}
