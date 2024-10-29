import Interfaces.InventoryService;
import Interfaces.PaymentService;
import Interfaces.TaxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private InventoryService mockInventoryService;
    private PaymentService mockPaymentService;
    private TaxService mockTaxService;
    private OrderService orderService;

    @BeforeEach
    void setUp(){
        mockInventoryService = mock(InventoryService.class);
        mockPaymentService = mock(PaymentService.class);
        mockTaxService = mock(TaxService.class);
        orderService= new OrderService(mockInventoryService, mockPaymentService, mockTaxService);
    }

    @Test
    @DisplayName("Successful order Placement")
    void  testSuccessfulOrder(){
        when(mockInventoryService.checkAvailibility("123",10 )).thenReturn(true);
        when(mockPaymentService.processPayment("123", 200.0)).thenReturn(true);
        when(mockTaxService.calculateTax("123",100.0)).thenReturn(100.0);
        assertTrue(orderService.placeOrder("123", 10 , 100.0));
    }

    @Test
    @DisplayName("Order fails when product is unavailable")
    void testUnavailableProduct(){
        //Mock that the availability for the product is false
        when(mockInventoryService.checkAvailibility("1234", 5)).thenReturn(false);
        assertFalse(orderService.placeOrder("1234",5,100));
    }

    @Test
    @DisplayName("Order fails when payment is declined")
    void testPaymentDeclined(){
        when(mockInventoryService.checkAvailibility("1234", 5)).thenReturn(true);
        when(mockTaxService.calculateTax("1234",5)).thenReturn(10.0);
        when(mockPaymentService.processPayment("1234", 110.0)).thenReturn(false);
        assertFalse(orderService.placeOrder("1234",5,100));
    }

    @Test
    @DisplayName("Order fails for zero quantity")
    void testZeroQuantity(){
        assertFalse(orderService.placeOrder("1234",5,100));
    }

    @Test
    @DisplayName("Simulate inventory decrease on each order placement")
    void testnventoryDecreaseOnOrderPlacement(){
        //Need to mock Tax and procees payment
        when(mockPaymentService.processPayment("123", 200.0)).thenReturn(true);
        when(mockTaxService.calculateTax("123",100.0)).thenReturn(100.0);
        //Assume initial stock of 5
        AtomicInteger stock = new AtomicInteger(5);

        doAnswer(invocation -> {
            int quantity = invocation.getArgument(1);
            if(stock.get() >= quantity){
                stock.addAndGet(-quantity);
                return true;
            }
            return false;
        }).when(mockInventoryService).checkAvailibility(eq("123"), anyInt());

        assertTrue(orderService.placeOrder("123", 2 , 100.0));
        assertTrue(orderService.placeOrder("123", 2 , 100.0));
        assertFalse(orderService.placeOrder("123", 2 , 100.0));

        //Test
        //assertTrue(orderService.placeOrder("123", 2 , 100.0));
    }

    @Test
    @DisplayName("Dynamic tax calculation based on price")
    void testDaynamicTaxCalculation(){
        doAnswer(inovation -> {
            double price = inovation.getArgument(1);
            return price > 100.0 ? price * 0.15 : price * 0.1;
        }).when(mockTaxService).calculateTax(eq("123"), anyDouble());

        assertEquals(8, mockTaxService.calculateTax("123", 80.0));
        assertEquals(30, mockTaxService.calculateTax("123", 200));
    }

    @Test
    @DisplayName("Order fails for negative price")
    void testNegativePrice(){
        when(mockInventoryService.checkAvailibility("123", 5)).thenReturn(true);
        when(mockTaxService.calculateTax("123",100.0)).thenReturn(100.0);
        assertFalse(orderService.placeOrder("123", -1 , 100.0));
    }

    @Test
    @DisplayName("Handling excessice order quantity")
    void testExcenssiveQuantity() {
        assertThrows(ExcessiveQuantityException.class, () -> orderService.placeOrder("123", 1000, 100.0));
    }

    @Test
    @DisplayName("Database transaction error during order")
    void testDatabaseTransactionError(){
        doThrow(new RuntimeException("Databse error")).when(mockInventoryService).checkAvailibility("123", 1);
        assertThrows(RuntimeException.class, () -> orderService.placeOrder("123", 1, 100.0));
    }

    @Test
    @DisplayName("Order cancellation")
    void testOrderCancellation(){
        when(mockInventoryService.restock("123", 5)).thenReturn(true);
        when(mockPaymentService.processPayment("123", -500.0)).thenReturn(true);
        assertTrue(orderService.cancelOrder("123", 5, 100));
    }

    @Test
    @DisplayName("Free Item Order")
    void testFreeItemOrder(){
        when(mockTaxService.calculateTax("freeProduct",0)).thenReturn(0.0);
        when(mockInventoryService.checkAvailibility("freeProduct", 1)).thenReturn(true);
        when(mockPaymentService.processPayment("freeProduct", 0.0)).thenReturn(true);
        assertTrue(orderService.placeOrder("freeProduct", 1, 0.0));
    }
}