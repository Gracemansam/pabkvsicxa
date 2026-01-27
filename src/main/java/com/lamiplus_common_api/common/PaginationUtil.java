package com.lamiplus_common_api.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class PaginationUtil {
    private static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";
    private static final String HEADER_LINK_FORMAT = "<{0}>; rel=\"{1}\"";

    private PaginationUtil() {
    }

    public static <T> HttpHeaders generatePaginationHttpHeaders(UriComponentsBuilder uriBuilder, Page<T> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_X_TOTAL_COUNT, Long.toString(page.getTotalElements()));
        List<String> allowedHeaders = new ArrayList<>();
        allowedHeaders.add(HEADER_X_TOTAL_COUNT);
        headers.setAccessControlExposeHeaders(allowedHeaders);
        int pageNumber = page.getNumber();
        int pageSize = page.getSize();
        StringBuilder link = new StringBuilder();
        if (pageNumber < page.getTotalPages() - 1) {
            link.append(prepareLink(uriBuilder, pageNumber + 1, pageSize, "next")).append(",");
        }

        if (pageNumber > 0) {
            link.append(prepareLink(uriBuilder, pageNumber - 1, pageSize, "prev")).append(",");
        }

        link.append(prepareLink(uriBuilder, page.getTotalPages() - 1, pageSize, "last")).append(",").append(prepareLink(uriBuilder, 0, pageSize, "first"));
        headers.add("Link", link.toString());
        return headers;
    }

    public static PageDTO generatePagination(Page page, List records) {
        long totalRecords = page.getTotalElements();
        int pageNumber = page.getNumber();
        int pageSize = page.getSize();
        int totalPages = page.getTotalPages();
        return PageDTO.builder().totalRecords(totalRecords)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .records(records).build();
    }


    public static PageDTO generatePagination(Page page) {
        long totalRecords = page.getTotalElements();
        int pageNumber = page.getNumber();
        int pageSize = page.getSize();
        int totalPages = page.getTotalPages();
        return PageDTO.builder().totalRecords(totalRecords)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .records(page.toList()).build();
    }

    private static String prepareLink(UriComponentsBuilder uriBuilder, int pageNumber, int pageSize, String relType) {
        return MessageFormat.format(HEADER_LINK_FORMAT, preparePageUri(uriBuilder, pageNumber, pageSize), relType);
    }

    private static String preparePageUri(UriComponentsBuilder uriBuilder, int pageNumber, int pageSize) {
        return uriBuilder.replaceQueryParam("page", Integer.toString(pageNumber)).replaceQueryParam("size", Integer.toString(pageSize)).toUriString().replace(",", "%2C").replace(";", "%3B");
    }

    public static Sort validateSort(Sort sort, List<String> allowed) {
        // Filter only valid properties
        List<Sort.Order> validOrders = sort.stream()
                .filter(order -> allowed.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .toList();

        if (!validOrders.isEmpty()) {
            //Use the valid ones
            return Sort.by(validOrders);
        } else {
            // Fallback: use the first allowed property (default ascending)
            return Sort.by(allowed.getFirst());
        }
    }

    public static Pageable buildPageable(Pageable pageable, Sort safeSort){
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                safeSort
        );
    }
}

