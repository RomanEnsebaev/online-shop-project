package org.onlineshop.services;

import org.onlineshop.dao.ProductDao;
import org.onlineshop.dto.ProductDto;
import org.onlineshop.dto.utility.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductDao dao;

    public ProductService(ProductDao dao) {
        this.dao = dao;
    }

    public List<ProductDto> catalog() {
        return dao.findAllActive()
                .stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    public void add(ProductDto dto) {
        dao.save(ProductMapper.toEntity(dto));
    }
}
