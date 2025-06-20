package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Data
@Schema(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @Schema(
            description = "用户名",
            example = "admin",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度在3-20个字符之间")
    private String username;

    @Schema(
            description = "密码",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度在6-30个字符之间")
    private String password;
}