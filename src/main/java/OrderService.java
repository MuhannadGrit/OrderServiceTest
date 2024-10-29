import Interfaces.InventoryService;
import Interfaces.PaymentService;
import Interfaces.TaxService;

public class OrderService {
    private InventoryService inventoryService;
    private PaymentService paymentService;
    private TaxService taxService;
    private static final int MAX_ORDER_QUANTITY = 1000;

    public OrderService(InventoryService inventoryService, PaymentService paymentService, TaxService taxService) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.taxService = taxService;
    }

    public boolean placeOrder(String productId, int quantity, double price) {
        if (quantity >= MAX_ORDER_QUANTITY) {
            throw new ExcessiveQuantityException("Order quantity exceed maximum quantity, please contact Support");
        }
       if(!isProductAvailable(productId, quantity)){
           return false;
       }
        double totalCost = calculateTotalCost(productId, price);
        if(totalCost < 0){
            return false;
        }
       return processPayment(productId, totalCost);
    }

    private boolean isProductAvailable(String productId, int quantity) {
        return  inventoryService.checkAvailibility(productId, quantity);
    }

    private boolean processPayment(String productId, double price) {
        return paymentService.processPayment(productId, price);
    }

    private double calculateTotalCost(String productId, double price) {
        double tax = taxService.calculateTax(productId, price);
        return tax >= 0 ?  price + tax : -1;
    }

    public boolean cancelOrder(String productId, int quantity, double price) {
        if(inventoryService.restock(productId,quantity)){
            double refundAmount = -price * quantity;
            return  paymentService.processPayment(productId, refundAmount);
        }
        return false;
    }
}
