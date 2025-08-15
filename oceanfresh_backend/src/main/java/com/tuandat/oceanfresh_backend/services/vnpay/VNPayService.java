package com.tuandat.oceanfresh_backend.services.vnpay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuandat.oceanfresh_backend.components.VNPayConfig;
import com.tuandat.oceanfresh_backend.components.VNPayUtils;
import com.tuandat.oceanfresh_backend.dtos.payment.PaymentDTO;
import com.tuandat.oceanfresh_backend.dtos.payment.PaymentQueryDTO;
import com.tuandat.oceanfresh_backend.dtos.payment.PaymentRefundDTO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VNPayService implements IVNPayService {

    private static final Logger logger = LoggerFactory.getLogger(VNPayService.class);

    private final VNPayConfig vnPayConfig;
    private final VNPayUtils vnPayUtils;

    @Override
    public String createPaymentUrl(PaymentDTO paymentDto, HttpServletRequest httpRequest) {
        String version = "2.1.0";
        String command = "pay";
        String orderType = "other";
        long amount = paymentDto.getAmount() * 100; // Số tiền cần nhân với 100
        String bankCode = paymentDto.getBankCode();

        String transactionReference = vnPayUtils.getRandomNumber(8); // Mã giao dịch
        String clientIpAddress = vnPayUtils.getIpAddress(httpRequest);

        String terminalCode = vnPayConfig.getVnpTmnCode();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", terminalCode);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            params.put("vnp_BankCode", bankCode);
        }
        params.put("vnp_TxnRef", transactionReference);
        params.put("vnp_OrderInfo", "Thanh toan don hang:" + transactionReference);
        params.put("vnp_OrderType", orderType);

        String locale = paymentDto.getLanguage();
        if (locale != null && !locale.isEmpty()) {
            params.put("vnp_Locale", locale);
        } else {
            params.put("vnp_Locale", "vn");
        }

        params.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
        params.put("vnp_IpAddr", clientIpAddress);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String createdDate = dateFormat.format(calendar.getTime());
        params.put("vnp_CreateDate", createdDate);

        calendar.add(Calendar.MINUTE, 15);
        String expirationDate = dateFormat.format(calendar.getTime());
        params.put("vnp_ExpireDate", expirationDate);

        List<String> sortedFieldNames = new ArrayList<>(params.keySet());
        Collections.sort(sortedFieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder queryData = new StringBuilder();

        for (Iterator<String> iterator = sortedFieldNames.iterator(); iterator.hasNext();) {
            String fieldName = iterator.next();
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                queryData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (iterator.hasNext()) {
                    hashData.append('&');
                    queryData.append('&');
                }
            }
        }

        String secureHash = vnPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        queryData.append("&vnp_SecureHash=").append(secureHash);

        return vnPayConfig.getVnpPayUrl() + "?" + queryData;
    }

    @Override
    public String queryTransaction(PaymentQueryDTO queryDto, HttpServletRequest httpRequest) throws IOException {
        // Chuẩn bị tham số cho VNPay
        String requestId = vnPayUtils.getRandomNumber(8);
        String version = "2.1.0";
        String command = "querydr";
        String terminalCode = vnPayConfig.getVnpTmnCode();
        String transactionReference = queryDto.getOrderId();
        String transactionDate = queryDto.getTransDate();
        String createDate = vnPayUtils.getCurrentDateTime();
        String clientIpAddress = vnPayUtils.getIpAddress(httpRequest);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_RequestId", requestId);
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", terminalCode);
        params.put("vnp_TxnRef", transactionReference);
        params.put("vnp_OrderInfo", "Check transaction result for OrderId:" + transactionReference);
        params.put("vnp_TransactionDate", transactionDate);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_IpAddr", clientIpAddress);

        // Tạo chuỗi hash và chữ ký bảo mật
        String hashData = String.join("|", requestId, version, command,
                terminalCode, transactionReference, transactionDate, createDate, clientIpAddress, "Check transaction");
        String secureHash = vnPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        params.put("vnp_SecureHash", secureHash);

        // Gửi yêu cầu API đến VNPay
        URL apiUrl = new URL(vnPayConfig.getVnpApiUrl());
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
            writer.writeBytes(new ObjectMapper().writeValueAsString(params));
        }
        int responseCode = connection.getResponseCode();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            if (responseCode == 200) {
                return response.toString();
            } else {
                throw new RuntimeException("VNPay API Error: " + response.toString());
            }
        }
    }

    @Override
    public String refundTransaction(PaymentRefundDTO refundRequest) throws IOException {
        // Tạo các tham số cần thiết
        String requestId = vnPayUtils.getRandomNumber(8);
        String version = "2.1.0";
        String command = "refund";
        String terminalCode = vnPayConfig.getVnpTmnCode();
        String createDate = vnPayUtils.getCurrentDateTime();
        String orderInfo = "Hoan tien don hang: " + refundRequest.getOrderId();
        String transactionNo = refundRequest.getVnpTxnRef() != null ? refundRequest.getVnpTxnRef() : "";
        String transactionDate = refundRequest.getOriginalTransactionDate() != null ? 
                refundRequest.getOriginalTransactionDate() : refundRequest.getTransactionDate();

        // Tạo hash data theo đúng format VNPay (nối bằng dấu |)
        // Theo tài liệu: vnp_RequestId + "|" + vnp_Version + "|" + vnp_Command + "|" + vnp_TmnCode + "|" + 
        // vnp_TransactionType + "|" + vnp_TxnRef + "|" + vnp_Amount + "|" + vnp_TransactionNo + "|" + 
        // vnp_TransactionDate + "|" + vnp_CreateBy + "|" + vnp_CreateDate + "|" + vnp_IpAddr + "|" + vnp_OrderInfo
        String hashData = String.join("|", 
            requestId,
            version,
            command,
            terminalCode,
            refundRequest.getTransactionType(),
            refundRequest.getOrderId(),
            String.valueOf(refundRequest.getAmount() * 100),
            transactionNo,
            transactionDate,
            refundRequest.getCreatedBy(),
            createDate,
            refundRequest.getIpAddress(),
            orderInfo
        );

        // Tạo secure hash
        String secureHash = vnPayUtils.hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        // Build JSON payload theo đúng spec VNPay
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_RequestId", requestId);
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", terminalCode);
        params.put("vnp_TransactionType", refundRequest.getTransactionType());
        params.put("vnp_TxnRef", refundRequest.getOrderId());
        params.put("vnp_Amount", String.valueOf(refundRequest.getAmount() * 100));
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_TransactionNo", transactionNo);
        params.put("vnp_TransactionDate", transactionDate);
        params.put("vnp_CreateBy", refundRequest.getCreatedBy());
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_IpAddr", refundRequest.getIpAddress());
        params.put("vnp_SecureHash", secureHash);

        // Log để debug
        logger.info("=== VNPay Refund Request ===");
        logger.info("Request ID: {}", requestId);
        logger.info("Order ID: {}", refundRequest.getOrderId());
        logger.info("Amount: {} VND", refundRequest.getAmount());
        logger.info("Transaction Type: {}", refundRequest.getTransactionType());
        logger.info("Hash data: {}", hashData);
        logger.info("Secure hash: {}", secureHash);
        logger.debug("Secret key length: {}", vnPayConfig.getSecretKey().length());
        logger.debug("Request params: {}", params);

        // Gửi request đến VNPay
        URL apiUrl = URI.create(vnPayConfig.getVnpApiUrl()).toURL();
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "OceanFresh-Backend/1.0");
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        // Ghi dữ liệu JSON
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] jsonPayload = new ObjectMapper().writeValueAsBytes(params);
            outputStream.write(jsonPayload);
        }

        // Đọc response
        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? connection.getInputStream()
                                : connection.getErrorStream(),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        // Log response để debug
        logger.info("=== VNPay Refund Response ===");
        logger.info("Response code: {}", responseCode);
        logger.info("Response body: {}", response.toString());

        if (responseCode != HttpURLConnection.HTTP_OK) {
            logger.error("VNPay API Error - Code: {}, Response: {}", responseCode, response.toString());
            throw new RuntimeException(
                    "VNPay API Error. Response code: " + responseCode + ", Response: " + response.toString());
        }

        return response.toString();
    }

}
