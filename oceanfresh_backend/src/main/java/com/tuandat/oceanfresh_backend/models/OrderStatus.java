package com.tuandat.oceanfresh_backend.models;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED_BY_CUSTOMER,
    CANCELLED_BY_ADMIN,
    RETURNED
}
