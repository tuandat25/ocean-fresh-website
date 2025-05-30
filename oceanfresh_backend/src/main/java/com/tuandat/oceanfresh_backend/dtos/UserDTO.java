package com.tuandat.oceanfresh_backend.dtos;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("phone_number")
    private String phoneNumber = "";

    @JsonProperty("email")
    private String email = "";

    private String address = "";

    @NotBlank(message = "Password cannot be blank")
    private String password = "";

    @JsonProperty("retype_password")
    private String retypePassword = "";

    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @NotNull(message = "Role ID is required")
    @JsonProperty("role_id")
    //role admin not permitted
    private Long roleId;
}
