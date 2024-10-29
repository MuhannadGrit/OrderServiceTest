import Interfaces.TaxService;

public class Tax implements TaxService {
    @Override
    public double calculateTax(String productId, double price) {
        return 0;
    }
}
