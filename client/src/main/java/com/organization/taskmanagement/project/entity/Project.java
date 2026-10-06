package com.organization.taskmanagement.project.entity;

import com.organization.taskmanagement.organization.entity.Organization;
import com.organization.taskmanagement.user.entity.RoleName;
import com.organization.taskmanagement.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * Staff and customers assigned to the project. Membership is per role: once a project has
     * staff members, only they work on it (likewise for customers). With none of a role, that
     * role is open to everyone eligible, as before members existed.
     */
    @ManyToMany
    @JoinTable(name = "project_member",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    public boolean hasMembersWithRole(RoleName role) {
        return members.stream().anyMatch(member -> member.hasRole(role));
    }

    public boolean isMember(User user) {
        return members.stream().anyMatch(member -> member.getId().equals(user.getId()));
    }

    /** Whether this user may raise or be asked clarifications on the project. */
    public boolean admits(User user) {
        RoleName role = user.getRole();
        if (role == null || role == RoleName.ADMIN) {
            return false;
        }
        if (role == RoleName.CUSTOMER && user.getOrganization() != null
                && !user.getOrganization().getId().equals(organization.getId())) {
            return false;
        }
        return !hasMembersWithRole(role) || isMember(user);
    }
}
