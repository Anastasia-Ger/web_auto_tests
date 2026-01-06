package models;

public final class BankingTestData {
    private BankingTestData() {}

    // Transfer limits
    public static final int TRANSFER_VALID_LOWER = 9999;
    public static final int TRANSFER_VALID_BOUNDARY = 10000;
    public static final int TRANSFER_INVALID_UPPER = 10001;
    public static final int TRANSFER_VALID_AMOUNT = 1500;
    public static final int TEST_FUNDS_REQUIRED = 15000;

    // Deposit limits
    public static final int MAX_DEPOSIT = 5000;
    public static final int DEPOSIT_VALID_BELOW_MAX = 4999;
    public static final int DEPOSIT_VALID_MIN = 1;
    public static final int DEPOSIT_INVALID_ABOVE_MAX = 5001;
    public static final int DEPOSIT_INVALID_NEGATIVE = -1;
}
