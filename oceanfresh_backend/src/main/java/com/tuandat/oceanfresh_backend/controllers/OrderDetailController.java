// package com.tuandat.oceanfresh_backend.controllers;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.tuandat.oceanfresh_backend.dtos.OrderDetailDTO;
// import com.tuandat.oceanfresh_backend.exceptions.DataNotFoundException;
// import com.tuandat.oceanfresh_backend.responses.ResponseObject;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("${api.prefix}/order_details")
// @RequiredArgsConstructor
// public class OrderDetailController {
//     // private final I orderDetailService;
//     // private final LocalizationUtils localizationUtils;
//     //Thêm mới 1 order detail
//     @PostMapping("")
//     // @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
//     public ResponseEntity<ResponseObject> createOrderDetail(
//             @Valid  @RequestBody OrderDetailDTO orderDetailDTO) throws Exception {
//         // OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
//         // OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(newOrderDetail);
//         // return ResponseEntity.ok().body(
//         //         ResponseObject.builder()
//         //                 .message("Create order detail successfully")
//         //                 .status(HttpStatus.CREATED)
//         //                 .data(orderDetailResponse)
//         //                 .build()
//         // );
//         return ResponseEntity.ok().body(
//                 ResponseObject.builder()
//                         .message("Create order detail successfully")
//                         .status(HttpStatus.CREATED)
//                         .data(orderDetailDTO)
//                         .build()
//         );
//     }

    
//     @GetMapping("/{id}")
//     public ResponseEntity<?> getOrderDetail(
//             @Valid @PathVariable("id") Long id) throws DataNotFoundException {
//         // OrderDetail orderDetail = orderDetailService.getOrderDetail(id);
//         // OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(orderDetail);
//         // return ResponseEntity.ok().body(
//         //         ResponseObject.builder()
//         //                 .message("Get order detail successfully")
//         //                 .status(HttpStatus.OK)
//         //                 .data(orderDetailResponse)
//         //                 .build()
//         // );
//         return ResponseEntity.ok().body(
//                 ResponseObject.builder()
//                         .message("Get order detail successfully")
//                         .status(HttpStatus.OK)
//                         .data(id)
//                         .build()
//         );
//     }
//     //lấy ra danh sách các order_details của 1 order nào đó
//     @GetMapping("/order/{orderId}")
//     public ResponseEntity<ResponseObject> getOrderDetails(
//             @Valid @PathVariable("orderId") Long orderId
//     ) {
//         // List<OrderDetail> orderDetails = orderDetailService.findByOrderId(orderId);
//         // List<OrderDetailResponse> orderDetailResponses = orderDetails
//         //         .stream()
//         //         .map(OrderDetailResponse::fromOrderDetail)
//         //         .toList();
//         // return ResponseEntity.ok().body(
//         //         ResponseObject.builder()
//         //                 .message("Get order details by orderId successfully")
//         //                 .status(HttpStatus.OK)
//         //                 .data(orderDetailResponses)
//         //                 .build()
//         // );
//         return ResponseEntity.ok().body(
//                 ResponseObject.builder()
//                         .message("Get order details by orderId successfully")
//                         .status(HttpStatus.OK)
//                         .data(orderId)
//                         .build()
//         );
//     }
//     //cập nhật order detail
//     @PutMapping("/{id}")
//     // @Operation(security = { @SecurityRequirement(name = "bearer-key") })
//     // @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
//     public ResponseEntity<ResponseObject> updateOrderDetail(
//             @Valid @PathVariable("id") Long id,
//             @RequestBody OrderDetailDTO orderDetailDTO) throws DataNotFoundException, Exception {
//         // OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
//         // return ResponseEntity.ok().body(ResponseObject
//         //                 .builder()
//         //                 .data(orderDetail)
//         //                 .message("Update order detail successfully")
//         //                 .status(HttpStatus.OK)
//         //         .build());
//         return ResponseEntity.ok().body(ResponseObject
//                 .builder()
//                 .data(orderDetailDTO)
//                 .message("Update order detail successfully")
//                 .status(HttpStatus.OK)
//                 .build());
//     }
    
//     @DeleteMapping("/{id}")
//     // @Operation(security = { @SecurityRequirement(name = "bearer-key") })
//     // @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")

//     public ResponseEntity<ResponseObject> deleteOrderDetail(
//             @Valid @PathVariable("id") Long id) {
//         // orderDetailService.deleteById(id);
//         // return ResponseEntity.ok()
//         //         .body(ResponseObject.builder()
//         //                 .message(localizationUtils
//         //                         .getLocalizedMessage(MessageKeys.DELETE_ORDER_DETAIL_SUCCESSFULLY))
//         //                 .build());
//         return ResponseEntity.ok()
//                 .body(ResponseObject.builder()
//                         .message("Delete order detail successfully")
//                         .build());
//     }
// }
