package kitchenpos.domain;

import java.util.Objects;

public class OrderTable {
    private Long id;
    private Long tableGroupId;
    private int numberOfGuests;
    private boolean empty;

    public OrderTable() {
        this.numberOfGuests = 0;
        this.empty = true;
    }

    public OrderTable(Long id, Long tableGroupId, int numberOfGuests, boolean empty) {
        this.id = id;
        this.tableGroupId = tableGroupId;
        this.numberOfGuests = numberOfGuests;
        this.empty = empty;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getTableGroupId() {
        return tableGroupId;
    }

    public void setTableGroupId(final Long tableGroupId) {
        this.tableGroupId = tableGroupId;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(final int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(final boolean empty) {
        this.empty = empty;
    }

    public void changeEmpty(final boolean empty) {
        if (Objects.nonNull(this.tableGroupId)) {
            throw new IllegalArgumentException("그룹으로 지정된 테이블은 변경할 수 없습니다.");
        }
        this.empty = empty;
    }

    public void changeNumberOfGuests(int numberOfGuests) {
        if (numberOfGuests < 0) {
            throw new IllegalArgumentException("손님의 수는 음수일 수 없습니다.");
        }

        if (this.empty) {
            throw new IllegalArgumentException("비어있는 테이블에는 손님 수를 설정할 수 없습니다.");
        }

        this.numberOfGuests = numberOfGuests;
    }

    public void group(final Long tableGroupId) {
        if (Objects.nonNull(this.tableGroupId)) {
            throw new IllegalArgumentException("이미 그룹 지정이 된 테이블입니다.");
        }
        if (!this.empty) {
            throw new IllegalArgumentException("비어있지 않은 테이블은 그룹지정을 할 수 없습니다.");
        }
        this.tableGroupId = tableGroupId;
        this.empty = false;
    }
}
