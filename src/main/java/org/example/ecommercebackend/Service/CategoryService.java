package org.example.ecommercebackend.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommercebackend.DTO.RequestDTO.CategoryRequestDTO;
import org.example.ecommercebackend.DTO.ResponseDTO.CategoryResponseDTO;
import org.example.ecommercebackend.Entity.Category;
import org.example.ecommercebackend.Exception.BadRequestException;
import org.example.ecommercebackend.Exception.ResourceNotFoundException;
import org.example.ecommercebackend.Repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;


    // ============================================================
    // CREATE CATEGORY
    // ============================================================

    @CacheEvict(value = "allCategories", allEntries = true)
    public CategoryResponseDTO createCategory(
            CategoryRequestDTO categoryRequestDTO) {

        log.info(
                "Creating new category with name={}",
                categoryRequestDTO.getName()
        );

        if (categoryRepository.existsByName(
                categoryRequestDTO.getName())) {

            log.warn(
                    "Category creation failed. Category already exists with name={}",
                    categoryRequestDTO.getName()
            );

            throw new BadRequestException(
                    "Category with name '" +
                            categoryRequestDTO.getName() +
                            "' already exists"
            );
        }

        Category category =
                modelMapper.map(
                        categoryRequestDTO,
                        Category.class
                );

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        Category savedCategory =
                categoryRepository.save(category);

        log.info(
                "Category created successfully: categoryId={}, name={}",
                savedCategory.getId(),
                savedCategory.getName()
        );

        return modelMapper.map(
                savedCategory,
                CategoryResponseDTO.class
        );
    }


    // ============================================================
    // GET CATEGORY BY ID
    // ============================================================

    @Cacheable(value = "categories", key = "#categoryId")
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(
            Integer categoryId) {

        log.debug(
                "Fetching category by id={}",
                categoryId
        );

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Category not found with id={}",
                                    categoryId
                            );

                            return new ResourceNotFoundException(
                                    "Category not found with id: "
                                            + categoryId
                            );
                        });

        log.info(
                "Category fetched successfully: categoryId={}, name={}",
                category.getId(),
                category.getName()
        );

        return modelMapper.map(
                category,
                CategoryResponseDTO.class
        );
    }


    // ============================================================
    // GET CATEGORY BY NAME
    // ============================================================

    @Cacheable(value = "categoriesByName", key = "#name")
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryByName(
            String name) {

        log.debug(
                "Fetching category by name={}",
                name
        );

        Category category =
                categoryRepository.findByName(name)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Category not found with name={}",
                                    name
                            );

                            return new ResourceNotFoundException(
                                    "Category not found with name: "
                                            + name
                            );
                        });

        log.info(
                "Category fetched successfully by name: categoryId={}, name={}",
                category.getId(),
                category.getName()
        );

        return modelMapper.map(
                category,
                CategoryResponseDTO.class
        );
    }


    // ============================================================
    // GET ALL CATEGORIES
    // ============================================================

    @Cacheable(value = "allCategories")
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {

        log.debug("Fetching all categories");

        List<CategoryResponseDTO> categories =
                categoryRepository.findAll()
                        .stream()
                        .map(category ->
                                modelMapper.map(
                                        category,
                                        CategoryResponseDTO.class
                                )
                        )
                        .collect(Collectors.toList());

        log.info(
                "All categories fetched successfully: count={}",
                categories.size()
        );

        return categories;
    }


    // ============================================================
    // UPDATE CATEGORY
    // ============================================================

    @CacheEvict(
            value = {
                    "categories",
                    "categoriesByName",
                    "allCategories"
            },
            allEntries = true
    )
    public CategoryResponseDTO updateCategory(
            Integer categoryId,
            CategoryRequestDTO categoryRequestDTO) {

        log.info(
                "Updating category: categoryId={}, newName={}",
                categoryId,
                categoryRequestDTO.getName()
        );

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Category update failed. Category not found with id={}",
                                    categoryId
                            );

                            return new ResourceNotFoundException(
                                    "Category not found with id: "
                                            + categoryId
                            );
                        });

        if (!category.getName()
                .equals(categoryRequestDTO.getName())
                &&
                categoryRepository.existsByName(
                        categoryRequestDTO.getName())) {

            log.warn(
                    "Category update failed. Category name already exists: name={}",
                    categoryRequestDTO.getName()
            );

            throw new BadRequestException(
                    "Category with name '" +
                            categoryRequestDTO.getName() +
                            "' already exists"
            );
        }

        category.setName(
                categoryRequestDTO.getName()
        );

        category.setDescription(
                categoryRequestDTO.getDescription()
        );

        category.setUpdatedAt(
                LocalDateTime.now()
        );

        Category updatedCategory =
                categoryRepository.save(category);

        log.info(
                "Category updated successfully: categoryId={}, name={}",
                updatedCategory.getId(),
                updatedCategory.getName()
        );

        return modelMapper.map(
                updatedCategory,
                CategoryResponseDTO.class
        );
    }


    // ============================================================
    // DELETE CATEGORY
    // ============================================================

    @CacheEvict(
            value = {
                    "categories",
                    "categoriesByName",
                    "allCategories"
            },
            allEntries = true
    )
    public void deleteCategory(Integer categoryId) {

        log.info(
                "Deleting category: categoryId={}",
                categoryId
        );

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Category deletion failed. Category not found with id={}",
                                    categoryId
                            );

                            return new ResourceNotFoundException(
                                    "Category not found with id: "
                                            + categoryId
                            );
                        });

        categoryRepository.delete(category);

        log.info(
                "Category deleted successfully: categoryId={}, name={}",
                categoryId,
                category.getName()
        );
    }


    // ============================================================
    // GET CATEGORY ENTITY BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public Category getCategoryEntityById(
            Integer categoryId) {

        log.debug(
                "Fetching category entity by id={}",
                categoryId
        );

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {

                    log.warn(
                            "Category entity not found with id={}",
                            categoryId
                    );

                    return new ResourceNotFoundException(
                            "Category not found with id: "
                                    + categoryId
                    );
                });
    }
}