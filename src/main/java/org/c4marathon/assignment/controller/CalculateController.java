package org.c4marathon.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.model.Calculate;
import org.c4marathon.assignment.model.User;
import org.c4marathon.assignment.service.CalculateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static org.c4marathon.assignment.util.AccountType.*;

@RestController
@RequestMapping("/calculate")
@RequiredArgsConstructor
public class CalculateController {
    private final CalculateService calculateService;


    // 정산은 사용자가 요청할 수 있으며, 해당하는 사용자들은 확인 후 직접 송금해야 합니다.
    @PostMapping("/")
    public ResponseEntity<?> calculate(List<User> userList, BigDecimal totalBalance, String type) {
        // userList의 첫번째 사람이 정산을 신청하는 사람
        if (type.equals(RANDOM_CALCULATE)) {
            // 랜덤하게 정산
            Calculate calculate = calculateService.calculateBalanceRandomlyByUser(userList.get(0), userList, totalBalance);
            return ResponseEntity.ok().body(calculate);
        } else if (type.equals(EQUALLY_CALCULATE)) {
            // 동일하게 정산
            // userList의 사람들에게 동일하게 정산
            Calculate calculate = calculateService.calculateBalanceEquallyByUser(userList.get(0), userList, totalBalance);
            return ResponseEntity.ok().body(calculate);
        } else {
            return ResponseEntity.badRequest().body("Invalid type");
        }
    }
}
