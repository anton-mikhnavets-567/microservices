package com.example.userservice;

import com.example.userservice.models.dtos.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/getUserIDByEmail/{email}")
    public ResponseEntity<Integer> getUserIDByEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(userService.getUserIDByEmail(email));
    }

    @GetMapping("/getAllEmployeeIDs")
    public ResponseEntity<List<Integer>> getAllEmployeeIDs(){
        return ResponseEntity.ok().body(userService.getAllEmployeeIDs());
    }

    @GetMapping("/getAllEmployeeAndManagerIDs")
    public ResponseEntity<List<Integer>> getAllEmployeeAndManagerIDs(){
        return ResponseEntity.ok().body(userService.getAllEmployeeAndManagerIDs());
    }

    @GetMapping("/getEmailsByRole/{role}")
    public ResponseEntity<String[]> getEmailsByRole(@PathVariable String role){
        return ResponseEntity.ok().body(userService.getEmailsByRole(role));
    }

    @PostMapping("/getUserInfoDTOs")
    public ResponseEntity<Map<Integer, UserInfoDTO>> getUserInfoDTOs(@RequestBody Set<Integer> uniqueIDs){
        return ResponseEntity.ok().body(userService.getUserInfoDTOsMapped(uniqueIDs));
    }

    @PostMapping("/getUsernamesByIDs")
    public ResponseEntity<Map<Integer, String>> getUsernamesByIDs(@RequestBody Set<Integer> ids) {
        return ResponseEntity.ok().body(userService.getUsernamesByIDs(ids));
    }

    @GetMapping("/getEmailByID/{id}")
    public ResponseEntity<String> getEmailByID (@PathVariable Integer id){
        return ResponseEntity.ok().body(userService.getEmailByID(id));
    }

    @GetMapping("/getAllEngineers")
    public ResponseEntity<List<UserInfoDTO>> getAllEngineers(){
        return ResponseEntity.ok().body(userService.getAllEngineers());
    }
}
