package cn.lmao.cloudown.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.lmao.cloudown.model.entity.File;
import cn.lmao.cloudown.model.entity.User;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUser(User user);

    File findByName(String name);
    
    Optional<File> findByUserAndRelativePath(User user, String relativePath);
}
