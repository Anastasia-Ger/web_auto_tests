package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerProfileResponse extends BaseModel {
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Transaction> accounts;

    @Data
    public static class Transaction {
        private int id;
        private double amount;
        private String type;
        private  String timestamp;
        private long relatedAccountId;


    }

}
