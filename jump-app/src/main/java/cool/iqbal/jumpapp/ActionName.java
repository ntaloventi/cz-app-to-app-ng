package cool.iqbal.jumpapp;

public enum ActionName {
    Action_Open(1, "open"),
    Action_Check_Balance(2, "checkbalance"),
    Action_Transfer(3, "transfer"),
    Action_Cash_Withdrawal(4, "cashwithdrawal"),
    Action_Cash_Deposit(5, "cashdeposit");

    private final int id;
    private final String name;

    ActionName(int id, String name){
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
