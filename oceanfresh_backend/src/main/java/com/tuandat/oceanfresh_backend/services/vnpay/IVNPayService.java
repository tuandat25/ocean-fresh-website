package com.tuandat.oceanfresh_backend.services.vnpay;

import java.io.IOException;

import com.tuandat.oceanfresh_backend.dtos.payment.PaymentDTO;
import com.tuandat.oceanfresh_backend.dtos.payment.PaymentQueryDTO;
import com.tuandat.oceanfresh_backend.dtos.payment.PaymentRefundDTO;

import jakarta.servlet.http.HttpServletRequest;

public interface IVNPayService {
    String createPaymentUrl(PaymentDTO paymentRequest, HttpServletRequest request);
    String queryTransaction(PaymentQueryDTO paymentQueryDTO, HttpServletRequest request) throws IOException;
    String refundTransaction(PaymentRefundDTO refundDTO) throws IOException;
}
