package ita.tinybite.global.s3;

import ita.tinybite.global.response.APIResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static ita.tinybite.global.response.APIResponse.success;

@RestController
@RequestMapping("/api/v1/file")
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<?> upload(@RequestPart MultipartFile file) throws IOException {
        return success(s3Service.upload(file, "uploads/" + UUID.randomUUID()));
    }
}
