package ita.tinybite.global.s3;

import ita.tinybite.global.exception.BusinessException;
import ita.tinybite.global.exception.errorcode.BusinessErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName = "tinybite-bucket";

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(MultipartFile file) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {
            throw BusinessException.of(BusinessErrorCode.INVALID_FILE_TYPE);
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());

        String key = "uploads/" + UUID.randomUUID() + "." + ext;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(key)).toString();
    }
}
