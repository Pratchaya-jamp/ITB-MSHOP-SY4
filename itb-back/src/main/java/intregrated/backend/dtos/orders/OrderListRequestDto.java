package intregrated.backend.dtos.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderListRequestDto {
    @NotEmpty(message = "order list must not be empty")
    private List<@Valid OrderRequestDto> orders;
}
