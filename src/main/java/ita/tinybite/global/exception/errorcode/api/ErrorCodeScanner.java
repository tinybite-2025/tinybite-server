package ita.tinybite.global.exception.errorcode.api;

import ita.tinybite.global.exception.errorcode.ErrorCode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ErrorCodeScanner {

    private static final String ERRORCODE_PACKAGE = "ita.tinybite.global.exception.errorcode";

    public Map<String, List<ErrorCodeInfo>> scan() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AssignableTypeFilter(ErrorCode.class));

        Map<String, List<ErrorCodeInfo>> errorCodes = new LinkedHashMap<>();

        for (BeanDefinition bd : scanner.findCandidateComponents(ERRORCODE_PACKAGE)) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());

                if (!clazz.isEnum()) continue;

                List<ErrorCodeInfo> errorCodeInfos = new ArrayList<>();
                for (Object value : clazz.getEnumConstants()) {
                    ErrorCode ec = (ErrorCode) value;

                    errorCodeInfos.add(new ErrorCodeInfo(
                            ec.getHttpStatus(),
                            ec.getCode(),
                            ec.getMessage()));
                }

                errorCodes.put(bd.getBeanClassName(), errorCodeInfos);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return errorCodes;
    }
}
