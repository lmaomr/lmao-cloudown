package cn.lmao.cloudown.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cn.lmao.cloudown.model.entity.File;
import cn.lmao.cloudown.model.entity.User;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUser(User user);

    File findByUserAndName(User user, String name);

    Optional<File> findByUserAndId(User user, Long id);

    @Query("SELECT f FROM File f WHERE f.relativePath LIKE CONCAT(:prefix, '%') AND LENGTH(f.relativePath) > LENGTH(:prefix)")
    List<File> findByRelativePathStartingWith(String prefix);
}
