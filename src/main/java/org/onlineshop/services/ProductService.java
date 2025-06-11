package org.onlineshop.services;

import org.onlineshop.dao.ProductDao;
import org.onlineshop.dto.ProductDto;
import org.onlineshop.dto.mappers.ProductMapper;
import org.onlineshop.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ProductService {

    private final ProductDao dao;
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductDao dao) {
        this.dao = dao;
    }

    public List<ProductDto> catalog() {
        try {
            return dao.findAllActive()
                    .stream()
                    .map(ProductMapper::toDto)
                    .toList();
        } catch (Exception ex) {
            log.error("ProductService.listActiveProducts failed", ex);
            throw new RuntimeException("Не удалось получить список продуктов. Попробуйте позже.", ex);
        }
    }

    public ProductDto find(int id) {
        try {
            Product p = dao.find(id);
            return p == null ? null : ProductMapper.toDto(p);
        } catch (RuntimeException ex) {
            log.error("ProductService.find failed for id={}", id, ex);
            throw new RuntimeException("Не удалось найти продукт. Попробуйте позже.", ex);
        }
    }

    @Transactional
    public void add(ProductDto dto) {
        try {
            dao.save(ProductMapper.toEntity(dto));
        } catch (RuntimeException ex) {
            log.error("ProductService.create failed for dto={}", dto, ex);
            throw new RuntimeException("Не удалось сохранить продукт. Попробуйте позже.", ex);
        }
    }

    @Transactional
    public void update(ProductDto dto) {
        try {
            dao.update(ProductMapper.toEntity(dto));
        } catch (RuntimeException ex) {
            log.error("ProductService.update failed for dto={}", dto, ex);
            throw new RuntimeException("Не удалось обновить продукт. Попробуйте позже.", ex);
        }
    }

    @Transactional
    public void delete(int id) {
        try {
            dao.delete(id);
        } catch (RuntimeException ex) {
            log.error("ProductService.delete failed for id={}", id, ex);
            throw new RuntimeException("Не удалось удалить продукт. Попробуйте позже.", ex);
        }
    }
}
