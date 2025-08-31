package cn.lmao.cloudown.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "file")
public class File {
    @Id
    @Column(name = "file_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String name;

    @Column(name = "file_path", nullable = false)
    private String path;

    //相对路径
    @Column(name = "relative_path", nullable = false)
    private String relativePath = "/";

    //缩略图路径
    @Column(name = "thumbnail_path")
    private String thumbnailPath;

    //文件hash
    @Column(name = "file_hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "file_size", nullable = false)
    private Long size;

    @Column(name = "file_type")
    private String type;

    // 新增文件状态标识
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", length = 20) // 确保长度足够
    private FileStatus status = FileStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(name = "create_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime updateTime = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    //构造函数
    public File(String name, String path, String relativePath, String type, User user) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath == null ? "/" : relativePath;
        this.type = type;
        this.user = user;
        // 为hash和size设置默认值，避免null值
        this.hash = "default_" + System.currentTimeMillis();
        this.size = 0L;
    }

    // 文件状态枚举
    public enum FileStatus {
        ACTIVE,    // 活跃可用
        DELETED,   // 已删除
        ARCHIVED,   // 已归档
        UPLOADING   // 上传中
    }


}
