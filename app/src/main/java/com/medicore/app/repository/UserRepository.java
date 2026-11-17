package com.medicore.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medicore.app.models.User;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByDocumentNumberAndPassword(String documentNumber, String password);
    User findByDocumentNumber(String documentNumber);
    void deleteByDocumentNumber(String documentNumber);
}
