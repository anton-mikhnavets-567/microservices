package com.example.userservice.models.dtos;


import com.example.userservice.models.entities.User;
import com.example.userservice.models.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserInfoDTO{

    public UserInfoDTO(User u){
        this.id = u.getId();
        this.email = u.getEmail();
        this.role = u.getRole();
        this.username = u.getFirstName();
    }

    Integer id;
    String email;
    UserRole role;
    String username;
}
