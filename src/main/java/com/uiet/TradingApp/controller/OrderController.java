package com.uiet.TradingApp.controller;

import com.uiet.TradingApp.entity.Order;
import com.uiet.TradingApp.repository.OrderRepository;
import com.uiet.TradingApp.service.OrderService;
import com.uiet.TradingApp.utils.JwtUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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
public class OrderController {
  // TODO add from time search
  @Autowired private OrderService orderService;
  @Autowired private OrderRepository orderRepository;
  @Autowired private JwtUtil jwtUtil;

  @PostMapping("/buy-order")
  public ResponseEntity<?> buyOrder(@RequestBody Order order) {
    try {
      orderService.placeBuyOrder(order);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>("ERROR: " + e, HttpStatus.BAD_REQUEST);
    }
  }

  @PostMapping("/sell-order")
  public ResponseEntity<?> sellOrder(@RequestBody Order order) {
    try {
      orderService.placeSellOrder(order);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>("ERROR: " + e, HttpStatus.BAD_REQUEST);
    }
  }

  @DeleteMapping("/cancel-order/{orderId}")
  public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
    try {
      Optional<Order> order = orderRepository.findById(orderId);
      if (!order.isPresent()) {
        throw new RuntimeException("Order not found");
      } else {
        orderService.cancelOrder(order.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
    } catch (Exception e) {
      return new ResponseEntity<>("ERROR: " + e, HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/userId/{userId}")
  public ResponseEntity<?> findOrdersByUserId(@PathVariable Long userId) {

    try {
      List<Order> order = orderRepository.findByUser_id(userId);
      if (order.isEmpty()) {
        throw new RuntimeException("Order not found/User Id is incorrect");
      }
      return ResponseEntity.ok(order);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR: " + e);
    }
  }

  @GetMapping("/userName/{userName}")
  public ResponseEntity<?> findOrdersByUserName(@PathVariable String userName) {

    try {
      List<Order> order = orderRepository.findByUser_UserName(userName);
      if (order.isEmpty()) {
        throw new RuntimeException(
            "Order not found/User UserName is incorrect");
      }
      return ResponseEntity.ok(order);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ERROR: " + e);
    }
  }

  @GetMapping("/all")
  public ResponseEntity<?>
  getAllOrders(@RequestHeader("Authorization") String authHeader) {
    try {
      String username = jwtUtil.extractUsername(authHeader);
      return ResponseEntity.ok(orderRepository.findByUser_UserName(username));
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
  }
}
