package kitchenpos.dto;

import java.time.LocalDateTime;
import java.util.List;

import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;

public class TableGroupResponseDto {
    private final Long id;
    private final LocalDateTime createdDate;
    private final List<OrderTableResponseDto> orderTables;

    public static TableGroupResponseDto from(TableGroup tableGroup, List<OrderTable> orderTables) {
        return new TableGroupResponseDto(tableGroup.getId(), tableGroup.getCreatedDate(), OrderTableResponseDto.listOf(orderTables));
    }

    public TableGroupResponseDto(Long id, LocalDateTime createdDate,
        List<OrderTableResponseDto> orderTables) {
        this.id = id;
        this.createdDate = createdDate;
        this.orderTables = orderTables;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<OrderTableResponseDto> getOrderTables() {
        return orderTables;
    }
}
