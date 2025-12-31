package ita.tinybite.global.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.BusinessErrorCode;
import ita.tinybite.global.health.LoginReqDto;
import ita.tinybite.global.health.service.AuthTestService;
import ita.tinybite.global.response.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static ita.tinybite.global.response.APIResponse.*;

@RestController
public class HealthCheckController {

    private final AuthTestService authTestService;

    public HealthCheckController(AuthTestService authTestService) {
        this.authTestService = authTestService;
    }

    @GetMapping("/test/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }

    @GetMapping("/api/v1/test/health")
    public APIResponse<?> test() {
        return success("test");
    }

    @GetMapping("/api/v1/test/error/business")
    public APIResponse<?> businessError() {
        throw BusinessException.of(BusinessErrorCode.MEMBER_NOT_FOUND);
    }

    @GetMapping("/api/v1/test/error/common")
    public APIResponse<?> commonError() throws Exception {
        throw new Exception("INTERNAL_SERVER_ERROR");
    }

    @PostMapping("/api/v1/test/login")
    @Operation(summary = "백엔드에서 유저 인증을 위한 API")
    public APIResponse<?> login(@RequestBody LoginReqDto req) {
        return success(authTestService.getUser(req.email()));
    }
}
