package ru.practicum.category.dal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories", indexes = {@Index(name = "idx_categories_cat_name", columnList = "cat_name")})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private Long id;

    @Size(min = 1, max = 50)
    @NotEmpty
    @Column(name = "cat_name", length = 50, nullable = false, unique = true)
    private String name;

}