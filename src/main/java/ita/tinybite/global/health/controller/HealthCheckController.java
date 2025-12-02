package ita.tinybite.global.health.controller;

import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.BusinessErrorCode;
import ita.tinybite.global.response.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }

    @GetMapping("/health-check")
    public APIResponse<?> test() {
        return APIResponse.success("test");
    }

    @GetMapping("/business-error")
    public APIResponse<?> businessError() {
        throw BusinessException.of(BusinessErrorCode.MEMBER_NOT_FOUND);
    }

    @GetMapping("/common-error")
    public APIResponse<?> commonError() throws Exception {
        throw new Exception("INTERNAL_SERVER_ERROR");
    }
}
