package greencity.dto.employee;

import greencity.annotations.ValidPhoneNumber;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.position.PositionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddEmployeeDto {
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String firstName;
    @NotNull
    @Pattern(regexp = "[ЁёІіЇїҐґЄєА-Яа-яA-Za-z-'\\s.]{1,30}")
    private String lastName;
    @NotNull
    @ValidPhoneNumber
    private String phoneNumber;
    @Email
    private String email;
    @NotEmpty
    private List<PositionDto> employeePositions;
    @NotEmpty
    private List<ReceivingStationDto> receivingStations;
}
