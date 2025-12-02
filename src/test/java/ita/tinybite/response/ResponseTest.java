package ita.tinybite.response;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ita.tinybite.global.exception.errorcode.BusinessErrorCode;
import ita.tinybite.global.exception.errorcode.CommonErrorCode;
import ita.tinybite.global.exception.errorcode.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ResponseTest {

    private static final String SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";
    private static final ErrorCode BUSINESS_ERRORCODE = BusinessErrorCode.MEMBER_NOT_FOUND;

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("응답 성공 시, APIResponse.success()의 리턴형식을 준수합니다.")
    public void success_response() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.message").value(SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("Business 에러 발생 시, APIResponse.businessError()의 리턴형식을 준수합니다.")
    public void business_error_response() throws Exception {
        mockMvc.perform(get("/business-error"))
                .andExpect(result -> assertNotEquals(200, result.getResponse().getStatus()))
                .andExpect(jsonPath("$.status").value(BUSINESS_ERRORCODE.getHttpStatus().value()))
                .andExpect(jsonPath("$.code").value(BUSINESS_ERRORCODE.getCode()))
                .andExpect(jsonPath("$.message").value(BUSINESS_ERRORCODE.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Common 에러 발생 시, APIResponse.commonError()의 리턴형식을 준수합니다.")
    public void commonError() throws Exception {
        mockMvc.perform(get("/common-error"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(
                        jsonPath("$.message")
                                .value(CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
