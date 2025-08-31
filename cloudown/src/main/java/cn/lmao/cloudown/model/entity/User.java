package cn.lmao.cloudown.model.entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.lmao.cloudown.model.enums.UserRole;
import jakarta.persistence.CascadeType;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_email" ,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "nick_name", nullable = true)
    private String nickname;

    //头像
    @Column(name = "avatar_url")
    private String avatarUrl;

    //使用容量
    @Column(name = "used_capacity", nullable = false)
    private Long usedCapacity = 0L;

    //总容量 
    @Column(name = "total_capacity", nullable = false)
    private Long totalCapacity = 1024L * 1024L * 10L;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "role_id") 
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<File> files = new ArrayList<>();

    @Column(name = "create_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime updateTime;

    @Column(name = "last_login_time", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime lastLoginTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = createTime;
        this.nickname = this.email.split("@")[0];
        this.role = Role.assignRole(UserRole.USER.getRole());
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

}
