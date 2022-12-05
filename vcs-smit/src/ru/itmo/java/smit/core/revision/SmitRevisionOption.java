package ru.itmo.java.smit.core.revision;

public enum SmitRevisionOption {
    COMMIT_HASH,
    BRANCH,
    HEAD_N;

    private String optionInfo;

    public String getOptionInfo() {
        return optionInfo;
    }

    public void setOptionInfo(String optionInfo) {
        this.optionInfo = optionInfo;
    }
}

