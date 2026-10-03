package model;

/**
 * PaymentMode – maps to the PAYMENT_MODES table.
 */
public class PaymentMode {

    private int    modeId;
    private String modeName;

    // ── Constructors ─────────────────────────────────────────────────────────

    public PaymentMode() {}

    public PaymentMode(int modeId, String modeName) {
        this.modeId   = modeId;
        this.modeName = modeName;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public int getModeId() {
        return modeId;
    }

    public void setModeId(int modeId) {
        this.modeId = modeId;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    // ── toString – displayed in JComboBox ────────────────────────────────────

    @Override
    public String toString() {
        return modeName;
    }
}
