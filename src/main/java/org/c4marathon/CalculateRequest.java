package org.c4marathon;

import lombok.Data;
import org.c4marathon.assignment.model.User;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculateRequest {
    private List<User> userList;
    private BigDecimal totalBalance;
    private String type;
}
