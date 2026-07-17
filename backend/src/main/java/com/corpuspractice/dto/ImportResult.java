package com.corpuspractice.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int totalRows;
    private int newCount;
    private int skipCount;
    private List<String> errors = new ArrayList<>();
}
