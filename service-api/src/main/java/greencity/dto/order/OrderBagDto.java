package greencity.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBagDto {
    private Integer id;
    private Integer amount;
}
