package greencity.dto.order;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class OrderForAddressDto {
    @NotNull
    @Min(1)
    private Long id;
    @Length(max = 50)
    private String email;
    @Length(max = 30)
    private String firstname;
    @Length(max = 30)
    private String lastname;
    @Length(max = 9)
    private String phoneNumber;
}
