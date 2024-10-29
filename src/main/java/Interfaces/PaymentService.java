package Interfaces;

public interface PaymentService {
    boolean processPayment(String productId, double amount);
}
