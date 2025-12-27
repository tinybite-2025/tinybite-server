package ita.tinybite.domain.party.dto.response;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductLink {
    private String thumbnailImage;
    private String productName;
    private String url;
}
