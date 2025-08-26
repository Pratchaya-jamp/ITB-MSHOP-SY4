package intregrated.backend.dtos.Paginations;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponseDto<T> {
    private List<T> content;
    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalElements;
    private Boolean first;
    private Boolean last;
    private String sort;
}
