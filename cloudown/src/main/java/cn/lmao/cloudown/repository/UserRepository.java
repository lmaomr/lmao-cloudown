package cn.lmao.cloudown.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.lmao.cloudown.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    User getUserByEmail(String email);
}
