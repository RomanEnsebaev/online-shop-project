package org.onlineshop.dto.mappers;

import org.onlineshop.dto.OrderItemDto;
import org.onlineshop.model.OrderItem;

public class OrderItemMapper {
    public static OrderItemDto toDto(OrderItem entity) {
        if (entity == null) return null;
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(entity.getProductId());
        dto.setProductName(entity.getProductName());
        dto.setQuantity(entity.getQuantity());
        dto.setPrice(entity.getPrice());
        return dto;
    }

    public static OrderItem toEntity(OrderItemDto dto, int orderId) {
        if (dto == null) return null;
        OrderItem entity = new OrderItem();
        entity.setOrderId(orderId);
        entity.setProductId(dto.getProductId());
        entity.setProductName(dto.getProductName());
        entity.setQuantity(dto.getQuantity());
        entity.setPrice(dto.getPrice());
        return entity;
    }
}
