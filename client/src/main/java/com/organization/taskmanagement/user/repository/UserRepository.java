package com.organization.taskmanagement.user.repository;

import com.organization.taskmanagement.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUserName(String userName);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRoles_Name(String roleName);

    long countByRoles_Name(String roleName);

    /** Active users with a role, optionally limited to one organization. */
    @Query("""
            select u from User u join u.roles r
            where r.name = :roleName and u.active = true
              and (:organizationId is null or u.organization.id = :organizationId)
            order by u.fullName""")
    List<User> findActiveByRole(@Param("roleName") String roleName, @Param("organizationId") Long organizationId);

    /** Admin user list: optional role filter and free-text search on name, username and email. */
    @Query("""
            select distinct u from User u join u.roles r
            where (:roleName is null or r.name = :roleName)
              and (:search is null
                   or lower(u.fullName) like :search
                   or lower(u.userName) like :search
                   or lower(u.email) like :search)""")
    Page<User> search(@Param("roleName") String roleName, @Param("search") String search, Pageable pageable);
}
