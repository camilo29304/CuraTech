package com.medicore.app.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medicore.app.models.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{
    Role findByTitleNameIgnoreCase(String titleName);
}
