package com.example.to_dolist.data.mapper;

import com.example.to_dolist.data.local.entity.CategoryEntity;
import com.example.to_dolist.domain.model.Category;

public class CategoryMapper {

    public static Category toDomain(CategoryEntity entity) {
        return new Category(entity.getId(), entity.getName(), entity.getColorHex());
    }

    public static CategoryEntity toEntity(Category category) {
        CategoryEntity entity = new CategoryEntity(category.getName(), category.getColorHex());
        if (category.getId() != 0) entity.setId(category.getId());
        return entity;
    }
}
