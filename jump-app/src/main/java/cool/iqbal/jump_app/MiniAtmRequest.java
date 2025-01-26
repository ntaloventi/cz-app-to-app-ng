package cool.iqbal.jump_app;

public class MiniAtmRequest {
    private boolean launcher;
    private String callback;
    /*addition for transfer*/
    private int amount;
    private String beneBankCode;
    private String beneAccountNo;

    public boolean isLauncher() {
        return launcher;
    }

    public void setLauncher(boolean launcher) {
        this.launcher = launcher;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getBeneBankCode() {
        return beneBankCode;
    }

    public void setBeneBankCode(String beneBankCode) {
        this.beneBankCode = beneBankCode;
    }

    public String getBeneAccountNo() {
        return beneAccountNo;
    }

    public void setBeneAccountNo(String beneAccountNo) {
        this.beneAccountNo = beneAccountNo;
    }
}
