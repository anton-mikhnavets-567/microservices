package com.example.userservice;

import com.example.userservice.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(nativeQuery = true, value="SELECT * FROM \"user\" u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query(nativeQuery = true, value="SELECT u.id FROM \"user\" u WHERE u.email = :email")
    Integer findIDByEmail(@Param("email") String email);

    @Query(nativeQuery = true, value="SELECT u.email FROM \"user\" u WHERE u.role = CAST(:role AS \"userRole\")")
    String[] findEmailsByRole(@Param("role") String role);

    @Query("SELECT u from User u WHERE u.id = :id")
    User findUserById(@Param("id") Integer id);
    @Query(nativeQuery = true, value="SELECT u.id from \"user\" u where u.role = 'Employee'")
    List<Integer> findAllEmployeeIDs();

    @Query(nativeQuery = true, value="SELECT u.id from \"user\" u where u.role in ('Employee','Manager')")
    List<Integer> findAllEmployeeManagerIDs();

    @Query(nativeQuery = true, value="select * from \"user\" u where u.id in :uniqueIDs")
    List<User> findUserInfoDTOs(@Param("uniqueIDs") Set<Integer> uniqueIDs);
    @Query(nativeQuery = true, value="SELECT u.email from \"user\" u where u.id = :id")
    String findEmailByID(@Param("id") Integer id);
    @Query(nativeQuery = true, value="SELECT * from \"user\" where role = 'Engineer'")
    List<User> getAllEngineers();
}
