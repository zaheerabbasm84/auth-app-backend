package com.gen.auth.model;

import com.gen.auth.dto.UserDto;
import com.gen.auth.helperutils.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends Model implements UserDetails {

    private String firstName;
    private String lastName;
    private String userName;
    private String emailId;
    private String password;
    private String verificationToken;
    private String adUserId;
    private Boolean isEnabled = Boolean.TRUE;
    private String userType;
    private Boolean isAdmin;
    private Long organizationId;
    @Transient
    private Set<Permissions> permissions;

//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "user_roles",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id")
//    )
    @Transient
    private Set<String> roles = new HashSet<>();

    @Transient
    private List<UserRoles> userRoles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL;

    @Enumerated(EnumType.STRING)
    private Status status = Status.pending;

    public User merge(UserDto userDto) {
        if(userDto.getFirstName() != null){
            this.firstName=userDto.getFirstName();
        }
        if(userDto.getLastName() != null){
            this.lastName=userDto.getLastName();
        }
        if(userDto.getUserName() != null){
            this.userName=userDto.getUserName();
        }
        if(userDto.getEmailId() != null){
            this.emailId=userDto.getEmailId();
        }
        if(userDto.getPassword() != null){
            this.password=userDto.getPassword();
        }
//        if(userDto.getAdUserId() != null){
//            this.adUserId=userDto.getAdUserId();
//        }
        if(userDto.getProvider() != null){
            this.provider=userDto.getProvider();
        }
        if(userDto.getStatus() != null){
            this.status=userDto.getStatus();
        }
        return this;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(roles -> new SimpleGrantedAuthority("ROLE_"+roles)).toList();
    }

    @Override
    public String getUsername() {
        return this.emailId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//
//        Stream<GrantedAuthority> roleAuthorities = roles.stream()
//                .map(role -> new SimpleGrantedAuthority(role.getName()));
//
//        Stream<GrantedAuthority> permissionAuthorities = roles.stream()
//                .flatMap(role -> role.getPermissionSets().stream())
//                .flatMap(permissionSet -> permissionSet.getPermissions().stream())
//                .map(permission -> new SimpleGrantedAuthority(permission.getName()));
//
//        return Stream.concat(roleAuthorities, permissionAuthorities)
//                .toList();
//    }

    public String getUserName() {
        return userName;
    }
}
