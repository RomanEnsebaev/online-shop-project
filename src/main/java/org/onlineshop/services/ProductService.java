package org.onlineshop.services;

import org.onlineshop.dao.ProductDao;
import org.onlineshop.dto.ProductDto;
import org.onlineshop.dto.ProductPageDto;
import org.onlineshop.dto.mappers.ProductMapper;
import org.onlineshop.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

@Service
public class ProductService {

    private final ProductDao dao;
    private static final Logger log = LogManager.getLogger(ProductService.class);

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

    public ProductPageDto getProductsPage(int pageNum, int pageSize) throws SQLException, InterruptedException {
        try {
            int total = dao.countProducts();
            int totalPages = (int) Math.ceil((double) total / pageSize);
            if (pageNum < 1) pageNum = 1;
            if (pageNum > totalPages) pageNum = totalPages;

            int offset = (pageNum - 1) * pageSize;
            List<Product> products= dao.findProducts(pageSize, offset);

            List<ProductDto> dtos = products.stream()
                    .map(prod -> {
                        ProductDto dto = new ProductDto();
                        dto.setId(prod.getProductId());
                        dto.setName(prod.getName());
                        dto.setDescription(prod.getDescription());
                        dto.setPrice(prod.getPrice());
                        dto.setStockQty(prod.getStockQty());
                        return dto;
                    })
                    .toList();

            ProductPageDto page = new ProductPageDto();
            page.setProducts(dtos);
            page.setCurrentPage(pageNum);
            page.setTotalPages(totalPages);
            return page;
        } catch (SQLException ex) {
            log.error("ProductService.getProductsPage failed for pageNum={}, pageSize={}", pageNum, pageSize, ex);
            throw new RuntimeException("Не удалось получить страницу продуктов. Попробуйте позже.", ex);
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
