package greencity.dto.order;

import greencity.dto.address.AddressDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class OrderWithAddressesResponseDto {
    private List<AddressDto> addressList;
}
