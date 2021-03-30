package io.fortylines.hrcrm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "user_role")
public class Role implements GrantedAuthority {

    @Id
    @Column(name = "id")
    private Long id;

    private String roleName;

    @OneToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<User> roles;

    public Role() {
    }

    public Role(Long id) {
        this.id = id;
    }

    @Override
    public String getAuthority() {
        return getRoleName();
    }
}
