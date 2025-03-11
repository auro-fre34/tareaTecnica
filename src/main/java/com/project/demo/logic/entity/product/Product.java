package com.project.demo.logic.entity.product;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.demo.logic.entity.category.Category;
import jakarta.persistence.*;

@Entity
@Table(name= "product" )
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)

  private Long id;

  private String name;

  private String description;

  private int price;

  private int stock;


  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  @JsonBackReference
  private Category category;

  @JsonProperty("category_id")
  public Long getCategoryId() {
    return category != null ? category.getId() : null;
  }


  public Product(Long id, String name, String description, int price, int stock, Category category ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.stock = stock;
    this.category = category;
  }

  public Product() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

}