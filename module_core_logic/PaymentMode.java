package module_core_logic;

/**
 * MODULE 2 – Business Logic & Core Models
 * PaymentMode model representing methods of payment.
 */
public class PaymentMode {
    private int id;
    private String name;

    public PaymentMode() {}

    public PaymentMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
