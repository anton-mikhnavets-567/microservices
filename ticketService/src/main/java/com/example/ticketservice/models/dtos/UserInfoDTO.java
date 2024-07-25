package com.example.ticketservice.models.dtos;


public record UserInfoDTO (
        Integer id,
        String role,
        String email,
        String username)
{
}
