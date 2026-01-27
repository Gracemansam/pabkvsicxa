package com.lamiplus_common_api.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO<T> implements Serializable {
    private long totalRecords;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private transient List<T> records = new ArrayList<>();
}
