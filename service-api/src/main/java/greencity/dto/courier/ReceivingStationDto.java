package greencity.dto.courier;

import greencity.enums.StationStatus;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivingStationDto {
    @Min(1)
    private Long id;
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z0-9-'\\s.]{1,30}")
    private String name;

    private String createdBy;

    private LocalDate createDate;

    private StationStatus stationStatus;
}
