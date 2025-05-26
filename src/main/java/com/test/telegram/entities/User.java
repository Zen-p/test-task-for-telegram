package com.test.telegram.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;


@Entity
@Data
@AllArgsConstructor
public class User {

    @Id
    @SequenceGenerator (
        name = "user_sequence_generator",
        sequenceName = "user_sequence"
            )
    @GeneratedValue (
        strategy = GenerationType.SEQUENCE,
        generator = "user_sequence_generator"
            )
    private Long id;

    @Size(min = 2, max = 50, message = "First name cannot exceed 50 characters")
    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;


    @Size(min = 2, max = 50, message = "Last name cannot exceed 50 characters")
    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;


    @Size(min = 2, max = 50, message = "Username cannot exceed 50 characters")
    @NotBlank(message = "username name is required")
    @Column(name = "username", nullable = false, length = 50)
    private String username;
}
