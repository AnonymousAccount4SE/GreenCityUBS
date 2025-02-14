package greencity.dto.order;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class EcoNumberDto {
    private Set<String> ecoNumber;
}
