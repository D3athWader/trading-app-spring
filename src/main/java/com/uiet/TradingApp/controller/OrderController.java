package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.DTO.ApiResponse;
import com.uiet.TradingApp.DTO.NewOrder;
import com.uiet.TradingApp.entity.Order;
import com.uiet.TradingApp.repository.OrderRepository;
import com.uiet.TradingApp.service.OrderService;
import com.uiet.TradingApp.utils.JwtUtil;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
  // TODO: add from time search
  private final OrderService orderService;
  private final OrderRepository orderRepository;
  private final JwtUtil jwtUtil;
  private static final String ERROR_STRING = "ERROR: ";

  @PostMapping("/buy-order")
  public ResponseEntity<ApiResponse<Void>>
  buyOrder(@RequestBody NewOrder newOrder,
           @RequestHeader("Authorization") String authHeader) {
    authHeader = authHeader.substring(7);
    String username = jwtUtil.extractUsername(authHeader);
    newOrder.setUsername(username);
    Order order = orderService.createOrder(newOrder);
    try {
      orderService.placeBuyOrder(order);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(new ApiResponse<>(ERROR_STRING + e),
                                  HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/sell-order")
  public ResponseEntity<ApiResponse<Void>>

  sellOrder(@RequestBody NewOrder newOrder) {
    Order order = orderService.createOrder(newOrder);
    try {
      orderService.placeSellOrder(order);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(new ApiResponse<>(ERROR_STRING + e),
                                  HttpStatus.BAD_REQUEST);
    }
  }

  @DeleteMapping("/cancel-order/{orderId}")
  public ResponseEntity<ApiResponse<Void>>
  cancelOrder(@PathVariable Long orderId) {
    try {
      Optional<Order> order = orderRepository.findById(orderId);
      if (!order.isPresent()) {
        throw new RuntimeException("Order not found");
      } else {
        orderService.cancelOrder(order.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(new ApiResponse<>(ERROR_STRING + e),
                                  HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/userId/{userId}")
  public ResponseEntity<ApiResponse<List<Order>>>
  findOrdersByUserId(@PathVariable Long userId) {

    try {
      List<Order> order = orderRepository.findByUser_id(userId);
      if (order.isEmpty()) {
        throw new RuntimeException("Order not found/User Id is incorrect");
      }
      return ResponseEntity.ok(new ApiResponse<>(order));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(ERROR_STRING + e));
    }
  }

  @GetMapping("/userName/{userName}")
  public ResponseEntity<ApiResponse<List<Order>>>
  findOrdersByUserName(@PathVariable String userName) {

    try {
      List<Order> order = orderRepository.findByUser_UserName(userName);
      if (order.isEmpty()) {
        throw new RuntimeException(
            "Order not found/User UserName is incorrect");
      }
      return ResponseEntity.ok(new ApiResponse<>(order));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(ERROR_STRING + e));
    }
  }

  @GetMapping("/all")
  public ResponseEntity<ApiResponse<List<Order>>>
  getAllOrders(@RequestHeader("Authorization") String authHeader) {
    try {
      authHeader = authHeader.substring(7);
      String username = jwtUtil.extractUsername(authHeader);
      return ResponseEntity.ok(
          new ApiResponse<>(orderRepository.findByUser_UserName(username)));
    } catch (Exception e) {
      return new ResponseEntity<>(new ApiResponse<>("Exception: " + e),
                                  HttpStatus.FORBIDDEN);
    }
  }
}
