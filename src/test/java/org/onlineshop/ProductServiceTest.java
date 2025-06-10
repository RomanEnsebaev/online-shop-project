
package org.onlineshop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onlineshop.dao.ProductDao;
import org.onlineshop.dto.ProductDto;
import org.onlineshop.dto.mappers.ProductMapper;
import org.onlineshop.model.Product;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onlineshop.services.ProductService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductDao dao;
    @InjectMocks
    ProductService service;

    /**
     * Должен вернуть список DTO, когда DAO возвращает сущности.
     */
    @Test
    void shouldReturnDtoListWhenCatalogHasEntities() {
        Product p = new Product();
        p.setProductId(1);
        p.setName("A");
        p.setDescription("d");
        p.setPrice(new BigDecimal("1.11"));
        p.setStockQty(1);
        p.setActive(true);

        ProductDto dto = new ProductDto();
        dto.setId(1);
        dto.setName("A");
        dto.setDescription("d");
        dto.setPrice(new BigDecimal("1.11"));
        dto.setStockQty(1);
        dto.setActive(true);

        when(dao.findAllActive()).thenReturn(List.of(p));
        try (MockedStatic<ProductMapper> ms = mockStatic(ProductMapper.class)) {
            ms.when(() -> ProductMapper.toDto(p)).thenReturn(dto);

            List<ProductDto> result = service.catalog();

            assertEquals(1, result.size());
            assertEquals(dto, result.get(0));
            verify(dao).findAllActive();
            ms.verify(() -> ProductMapper.toDto(p));
        }
    }

    /**
     * Должен вернуть пустой список, когда DAO вернул пустой.
     */
    @Test
    void shouldReturnEmptyListWhenCatalogEmpty() {
        when(dao.findAllActive()).thenReturn(Collections.emptyList());

        List<ProductDto> result = service.catalog();

        assertTrue(result.isEmpty());
        verify(dao).findAllActive();
    }

    /**
     * Должен вызывать сохранение сущности при add().
     */
    @Test
    void shouldCallDaoSaveWhenAddingValidDto() {
        ProductDto dto = new ProductDto();
        dto.setId(0);
        dto.setName("N");
        dto.setDescription("d");
        dto.setPrice(new BigDecimal("2.22"));
        dto.setStockQty(2);
        dto.setActive(true);

        Product ent = new Product();
        try (MockedStatic<ProductMapper> ms = mockStatic(ProductMapper.class)) {
            ms.when(() -> ProductMapper.toEntity(dto)).thenReturn(ent);

            service.add(dto);

            ms.verify(() -> ProductMapper.toEntity(dto));
            verify(dao).save(ent);
        }
    }

    /**
     * Должен бросить NPE при add(null).
     */
    @Test
    void shouldThrowWhenAddNullDto() {
        assertThrows(NullPointerException.class, () -> service.add(null));
    }

    /**
     * Должен вернуть DTO при существующем продукте.
     */
    @Test
    void shouldReturnDtoWhenFindExisting() {
        Product p = new Product();
        p.setProductId(5);
        p.setName("X");
        p.setDescription("d");
        p.setPrice(new BigDecimal("5.55"));
        p.setStockQty(5);
        p.setActive(true);

        ProductDto dto = new ProductDto();
        dto.setId(5);
        dto.setName("X");
        dto.setDescription("d");
        dto.setPrice(new BigDecimal("5.55"));
        dto.setStockQty(5);
        dto.setActive(true);

        when(dao.find(5)).thenReturn(p);
        try (MockedStatic<ProductMapper> ms = mockStatic(ProductMapper.class)) {
            ms.when(() -> ProductMapper.toDto(p)).thenReturn(dto);

            assertEquals(dto, service.find(5));
            verify(dao).find(5);
            ms.verify(() -> ProductMapper.toDto(p));
        }
    }

    /**
     * Должен вернуть null при отсутствии продукта.
     */
    @Test
    void shouldReturnNullWhenFindNonExisting() {
        when(dao.find(99)).thenReturn(null);
        assertNull(service.find(99));
        verify(dao).find(99);
    }

    /**
     * Должен вызывать обновление сущности при update().
     */
    @Test
    void shouldCallDaoUpdateWhenUpdatingValidDto() {
        ProductDto dto = new ProductDto();
        dto.setId(2);
        dto.setName("U");
        dto.setDescription("dd");
        dto.setPrice(new BigDecimal("3.33"));
        dto.setStockQty(3);
        dto.setActive(false);

        Product ent = new Product();
        try (MockedStatic<ProductMapper> ms = mockStatic(ProductMapper.class)) {
            ms.when(() -> ProductMapper.toEntity(dto)).thenReturn(ent);

            service.update(dto);

            ms.verify(() -> ProductMapper.toEntity(dto));
            verify(dao).update(ent);
        }
    }

    /**
     * Должен бросить NPE при update(null).
     */
    @Test
    void shouldThrowWhenUpdateNullDto() {
        assertThrows(NullPointerException.class, () -> service.update(null));
    }

    /**
     * Должен вызывать удаление в DAO при delete().
     */
    @Test
    void shouldCallDaoDeleteWhenDeleting() {
        service.delete(7);
        verify(dao).delete(7);
    }
}
