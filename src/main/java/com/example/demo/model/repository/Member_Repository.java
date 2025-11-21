package com.example.demo.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.domain.Member;

@Repository
public interface Member_Repository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
}
