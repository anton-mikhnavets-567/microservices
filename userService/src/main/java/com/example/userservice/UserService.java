package com.example.userservice;

import com.example.userservice.models.dtos.UserInfoDTO;
import com.example.userservice.models.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    public Integer getUserIDByEmail(String email) {
        Integer userID = userRepository.findIDByEmail(email);
        System.out.println("aaaaaa" + userID);
        return userID;
    }

    public List<Integer> getAllEmployeeIDs() {
        return userRepository.findAllEmployeeIDs();
    }

    public List<Integer> getAllEmployeeAndManagerIDs() {
        return userRepository.findAllEmployeeManagerIDs();
    }


    public Map<Integer, UserInfoDTO> getUserInfoDTOsMapped(Set<Integer> uniqueIDs) {
        List<User> users = userRepository.findUserInfoDTOs(uniqueIDs);
        var dtos = new ArrayList<UserInfoDTO>();
        for (User u : users) {
            var dto = new UserInfoDTO(u);
            dtos.add(dto);
        }
        return dtos.stream()
                .filter(u -> uniqueIDs.contains(u.getId()))
                .collect(Collectors.toMap(UserInfoDTO::getId, u -> u));
    }

    public Map<Integer, String> getUsernamesByIDs(Set<Integer> ids) {
        List<User> users = userRepository.findUserInfoDTOs(ids);
        var map = new HashMap<Integer, String>();
        for(User u: users){
            map.put(u.getId(), u.getFirstName());
        }
        return map;
    }

    public String[] getEmailsByRole(String role) {
        return userRepository.findEmailsByRole(role);
    }

    public String getEmailByID(Integer id) {
        return userRepository.findEmailByID(id);
    }

    public List<UserInfoDTO> getAllEngineers() {
        List<User> engineers = userRepository.getAllEngineers();
        var dtos = new ArrayList<UserInfoDTO>();
        for (User u : engineers) {
            var dto = new UserInfoDTO(u);
            dtos.add(dto);
        }
        return dtos;
    }
}
