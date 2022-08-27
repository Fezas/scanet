package ru.fezas.scanet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortInfoScanner {
    private Integer workCount, errCount;

    public ShortInfoScanner(Integer workCount, Integer errCount) {
        this.workCount = workCount;
        this.errCount = errCount;
    }
}
