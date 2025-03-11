package com.project.demo.rest.product;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {
  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private UserRepository userRepository;

  //GET -POST - PATCH - PUT - DELETE

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> getProducts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      HttpServletRequest request
  ) {

    Pageable pageable = PageRequest.of(page-1, size);
    Page<Product> productsPage = productRepository.findAll(pageable);
    Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
    meta.setTotalPages(productsPage.getTotalPages());
    meta.setTotalElements(productsPage.getTotalElements());
    meta.setPageNumber(productsPage.getNumber() + 1);
    meta.setPageSize(productsPage.getSize());

    return new GlobalResponseHandler().handleResponse("Product retrieved successfully",
        productsPage.getContent(), HttpStatus.OK, meta);
  }

  @GetMapping("/{Id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<?> getProductById(@PathVariable Long Id, HttpServletRequest request) {
    Optional<Product> foundProduct = productRepository.findById(Id);
    if(foundProduct.isPresent())  {
      return new GlobalResponseHandler().handleResponse(
          "Product retrieved successfully",
          foundProduct.get(),
          HttpStatus.OK,
          request);
    } else {
      return new GlobalResponseHandler().handleResponse(
          "Product with id " + Id + " not found",
          HttpStatus.NOT_FOUND,
          request);
    }
  }

  @PostMapping
  @PreAuthorize("isAuthenticated() && hasAnyRole('SUPER_ADMIN')")
  public ResponseEntity<?> addProductToCategories(
      @RequestBody Product product,
      HttpServletRequest request) {
    //Buscar la categoria con el category_id
    Optional<Category> foundCategory = categoryRepository.findById(product.getCategory().getId());

    if (foundCategory.isPresent()) {
      Product toSaveProduct;

      //Si ya existe
      if (product.getId() != null) {
        Optional<Product> foundProduct = productRepository.findById(product.getId());
        toSaveProduct = foundProduct.orElse(new Product());
      } else {
        toSaveProduct = new Product();
      }

      //Asignar valores al producto
      toSaveProduct.setName(product.getName());
      toSaveProduct.setDescription(product.getDescription());
      toSaveProduct.setPrice(product.getPrice());
      toSaveProduct.setStock(product.getStock());
      toSaveProduct.setCategory(foundCategory.get());

      //Evitar duplicados
      if (foundCategory.get().getProducts().contains(toSaveProduct)) {
        return new GlobalResponseHandler().handleResponse(
            "Product already in this category",
            HttpStatus.BAD_REQUEST,
            request);
      }

      //Guardar el producto y la categoría
      productRepository.save(toSaveProduct);
      foundCategory.get().getProducts().add(toSaveProduct);
      categoryRepository.save(foundCategory.get());

      return new GlobalResponseHandler().handleResponse(
          "Product added to category " + foundCategory.get().getName(),
          foundCategory.get(),
          HttpStatus.OK,
          request);
    } else {
      return new GlobalResponseHandler().handleResponse(
          "Category not found",
          HttpStatus.NOT_FOUND,
          request);
    }
  }

  @PutMapping("/{Id}")
  @PreAuthorize("isAuthenticated() && hasAnyRole('SUPER_ADMIN')")
  public ResponseEntity<?> updateProduct(@PathVariable Long Id, @RequestBody Product product, HttpServletRequest request) {
    Optional<Product> foundProduct = productRepository.findById(Id);
    if (foundProduct.isPresent()) {
      Product existingProduct = foundProduct.get();
      existingProduct.setName(product.getName());
      existingProduct.setDescription(product.getDescription());
      existingProduct.setPrice(product.getPrice());
      existingProduct.setStock(product.getStock());

      productRepository.save(existingProduct);

      return new GlobalResponseHandler().handleResponse(
          "Product successfully updated",
          existingProduct,
          HttpStatus.OK,
          request);
    } else {
      return new GlobalResponseHandler().handleResponse(
          "Product with id " + Id + " not found",
          HttpStatus.NOT_FOUND,
          request);
    }
  }

  @DeleteMapping("/{Id}")
  @PreAuthorize("isAuthenticated() && hasAnyRole('SUPER_ADMIN')")
  public ResponseEntity<?> deleteProduct(@PathVariable Long Id, HttpServletRequest request) {
    Optional<Product> foundProduct = productRepository.findById(Id);
    if(foundProduct.isPresent()) {
      productRepository.deleteById(Id);
      return new GlobalResponseHandler().handleResponse(
          "Product successfully deleted",
          HttpStatus.OK,
          request);
    } else {
      return new GlobalResponseHandler().handleResponse(
          "Product with id " + Id + " not found",
          HttpStatus.NOT_FOUND,
          request);
    }
  }

}