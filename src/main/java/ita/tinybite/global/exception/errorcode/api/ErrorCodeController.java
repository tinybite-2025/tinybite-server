package ita.tinybite.global.exception.errorcode.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
public class ErrorCodeController {

    private final ErrorCodeScanner scanner;

    public ErrorCodeController(ErrorCodeScanner scanner) {
        this.scanner = scanner;
    }

    @GetMapping("/error-code")
    public String errorCode(Model model) {
        model.addAttribute("codes", scanner.scan());
        return "error-code";
    }
}
