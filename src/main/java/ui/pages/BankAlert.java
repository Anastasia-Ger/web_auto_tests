package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number:"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NEW_NAME_IS_SAME_AS_CURRENT("⚠\uFE0F New name is the same as the current one."),
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited"),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred"),
    NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER("❌ No user found with this account number."),
    TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    RECIPIENT_NAME_DOES_NOT_MATCH_REGISTERED_NAME("❌ The recipient name does not match the registered name."),
    PLEASE_FILL_ALL_FIELDS_AND_CONFIRM("❌ Please fill all fields and confirm."),
    PLEASE_ENTER_VALID_AMOUNT("❌ Please enter a valid amount."),
    PLEASE_ENTER_LESS_OR_EQUAL_TO_5000("❌ Please deposit less or equal to 5000$.");


    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
