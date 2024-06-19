package org.c4marathon.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/calculate")
@RequiredArgsConstructor
public class CalculateController {
    // 정산은 사용자가 요청할 수 있으며, 해당하는 사용자들은 확인 후 직접 송금해야 합니다.
    
}
