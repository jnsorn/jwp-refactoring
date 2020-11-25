package kitchenpos.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.dto.OrderCreateRequestDto;
import kitchenpos.dto.OrderLineCreateRequestDto;
import kitchenpos.dto.OrderResponseDto;
import kitchenpos.repository.MenuRepository;
import kitchenpos.repository.OrderLineItemRepository;
import kitchenpos.repository.OrderRepository;
import kitchenpos.repository.OrderTableRepository;

@Service
public class OrderService {
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderService(
        final MenuRepository menuRepository,
        final OrderRepository orderRepository,
        final OrderLineItemRepository orderLineItemRepository,
        final OrderTableRepository orderTableRepository
    ) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.orderTableRepository = orderTableRepository;
    }

    @Transactional
    public OrderResponseDto create(final OrderCreateRequestDto orderCreateRequest) {
        final List<OrderLineCreateRequestDto> orderLineCreateRequests = orderCreateRequest.getOrderLineCreateRequests();

        if (CollectionUtils.isEmpty(orderLineCreateRequests)) {
            throw new IllegalArgumentException("주문은 1개 이상의 메뉴를 포함해야 합니다.");
        }

        final List<Long> menuIds = orderLineCreateRequests.stream()
            .map(OrderLineCreateRequestDto::getMenuId)
            .collect(Collectors.toList());

        if (orderLineCreateRequests.size() != menuRepository.countAllByIds(menuIds)) {
            throw new IllegalArgumentException("존재하지 않는 메뉴로 주문할 수 없습니다.");
        }

        if (Objects.isNull(orderCreateRequest.getOrderTableId())) {
            throw new IllegalArgumentException("존재하지 않는 테이블에 주문할 수 없습니다.");
        }

        final OrderTable orderTable = orderTableRepository.findById(orderCreateRequest.getOrderTableId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테이블에 주문할 수 없습니다."));

        if (orderTable.isEmpty()) {
            throw new IllegalArgumentException("비어있는 테이블에는 주문할 수 없습니다.k");
        }

        final Order savedOrder = orderRepository.save(orderCreateRequest.toEntity());

        final Long orderId = savedOrder.getId();
        final List<OrderLineItem> savedOrderLineItems = new ArrayList<>();
        for (final OrderLineCreateRequestDto orderLineCreateRequest : orderLineCreateRequests) {
            savedOrderLineItems.add(orderLineItemRepository.save(orderLineCreateRequest.toEntity(orderId)));
        }

        OrderResponseDto orderResponseDto = OrderResponseDto.from(savedOrder, savedOrderLineItems);

        return orderResponseDto;
    }

    public List<OrderResponseDto> list() {
        final List<Order> orders = orderRepository.findAll();

        List<OrderResponseDto> orderResponses = new ArrayList<>();
        for (final Order order : orders) {
            List<OrderLineItem> orderLineItems = orderLineItemRepository.findAllByOrderId(order.getId());
            orderResponses.add(OrderResponseDto.from(order, orderLineItems));
        }

        return orderResponses;
    }

    @Transactional
    public OrderResponseDto changeOrderStatus(final Long orderId, final OrderStatus orderStatus) {
        final Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문번호입니다."));

        order.changeOrderStatus(orderStatus);
        Order savedOrder = orderRepository.save(order);

        List<OrderLineItem> orderLineItems = orderLineItemRepository.findAllByOrderId(savedOrder.getId());

        return OrderResponseDto.from(savedOrder, orderLineItems);
    }
}
