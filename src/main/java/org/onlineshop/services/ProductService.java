package org.onlineshop.services;

import org.onlineshop.dao.ProductDao;
import org.onlineshop.dto.ProductDto;
import org.onlineshop.dto.mappers.ProductMapper;
import org.onlineshop.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ProductDto find(int id) {
        Product p = dao.find(id);
        return p == null ? null : ProductMapper.toDto(p);
    }

    @Transactional
    public void update(ProductDto dto) {
        dao.update(ProductMapper.toEntity(dto));
    }

    @Transactional
    public void delete(int id) {
        dao.delete(id);
    }
}
