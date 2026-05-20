package com.brainbooster.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request body used to register a new user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  @Schema(
          description = "User nickname",
          example = "billy"
  )
  @NotBlank(message = "Nickname cannot be empty")
  private String nickname;

  @Schema(
          description = "User email address",
          example = "billy@example.com"
  )
  @NotBlank(message = "Email cannot be empty")
  @Email(message = "Must be a valid email format")
  private String email;

  @Schema(
          description = "User password",
          example = "test1234",
          accessMode = Schema.AccessMode.WRITE_ONLY
  )
  @NotBlank(message = "Password cannot be empty")
  private String password;
}
