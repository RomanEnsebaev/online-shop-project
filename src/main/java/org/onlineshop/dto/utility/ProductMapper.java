package org.onlineshop.dto.utility;

import org.onlineshop.dto.ProductDto;
import org.onlineshop.model.Product;

public class ProductMapper {

    private ProductMapper() {}

    public static ProductDto toDto(Product p) {
        ProductDto d = new ProductDto();
        d.setId(p.getProductId());
        d.setName(p.getName());
        d.setDescription(p.getDescription());
        d.setPrice(p.getPrice());
        d.setStockQty(p.getStockQty());
        d.setCreatedAt(p.getCreatedAt());
        return d;
    }

    public static Product toEntity(ProductDto d) {
        Product p = new Product();
        p.setProductId(d.getId());
        p.setName(d.getName());
        p.setDescription(d.getDescription());
        p.setPrice(d.getPrice());
        p.setStockQty(d.getStockQty());
        p.setActive(true);
        return p;
    }
}
