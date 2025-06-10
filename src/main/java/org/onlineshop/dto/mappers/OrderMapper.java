package org.onlineshop.dto.mappers;

import org.onlineshop.dto.OrderDto;
import org.onlineshop.dto.OrderItemDto;
import org.onlineshop.model.Order;
import org.onlineshop.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderDto toDto(Order order) {
        if (order == null) return null;
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotal(order.getTotal());
        List<OrderItemDto> items = order.getItems().stream()
                .map(OrderItemMapper::toDto)
                .collect(Collectors.toList());
        dto.setItems(items);
        return dto;
    }

    public static Order toEntity(OrderDto dto) {
        if (dto == null) return null;
        Order order = new Order();
        order.setId(dto.getId());
        order.setOrderDate(dto.getOrderDate());
        order.setTotal(dto.getTotal());
        List<OrderItem> items = dto.getItems().stream()
                .map(itemDto -> OrderItemMapper.toEntity(itemDto, dto.getId()))
                .collect(Collectors.toList());
        order.setItems(items);
        return order;
    }
}
