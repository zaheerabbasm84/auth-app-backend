package com.gen.auth.repository;

import com.gen.auth.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolesRepository extends JpaRepository<Roles,Long> {
}
