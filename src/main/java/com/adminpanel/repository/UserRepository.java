package com.adminpanel.repository;

import com.adminpanel.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	
    User findByUsername(String username);
    
    User findByEmail(String email);
    
//    User findByID(Long id);

}
