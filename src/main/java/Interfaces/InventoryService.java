package Interfaces;

public interface InventoryService {
  boolean checkAvailibility(String productId, int quantity) ;
  boolean restock(String productId, int quantity);
}
