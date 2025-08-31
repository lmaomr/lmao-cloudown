package cn.lmao.cloudown.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.lmao.cloudown.model.enums.ErrorOperationStatus;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于处理JWT令牌的生成、验证和解析
 */
@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expiration;
    private static final String TOKEN_PREFIX = "Bearer ";
    private final Logger log = LogUtil.getLogger();

    /**
     * 构造函数
     * @param secretKey JWT密钥
     * @param expiration 过期时间（毫秒）
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.expiration = expiration;
        log.info("JwtUtils初始化完成，过期时间设置为: {}ms", expiration);
    }

    /**
     * 生成JWT令牌
     * @param email 邮箱
     * @return JWT令牌字符串
     */
    public String generateToken(String email) {
        log.debug("开始为用户[{}]生成JWT令牌", email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + expiration);
        
        log.debug("令牌生成时间: {}, 过期时间: {}", now, expiryDate);
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
                
        log.debug("JWT令牌生成成功");
        return token;
    }

    /**
     * 从令牌中获取邮箱
     * @param authHeader JWT令牌
     * @return 邮箱
     */
    public String getEmailFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.warn(ErrorOperationStatus.TOKEN_FORMAT_ERROR.getMsg());
            throw new JwtException(ErrorOperationStatus.TOKEN_FORMAT_ERROR.getMsg());
        }

        String token = extractToken(authHeader);

        if (!validateToken(token)) {
            throw new JwtException(ErrorOperationStatus.TOKEN_INVALID.getMsg());
        }

        log.debug("开始从令牌中解析邮箱");
        String email = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
        log.debug("成功从令牌中解析出邮箱: {}", email);
        return email;
    }

    /**
     * 从Authorization头中提取令牌
     * @param authHeader Authorization头
     * @return JWT令牌
     * @throws JwtException 如果Authorization头格式无效
     */
    public String extractToken(String authHeader) {
        log.debug("开始从Authorization头中提取令牌");
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            log.warn(ErrorOperationStatus.TOKEN_FORMAT_ERROR.getMsg());
            throw new JwtException(ErrorOperationStatus.TOKEN_FORMAT_ERROR.getMsg());
        }
        String token = authHeader.substring(TOKEN_PREFIX.length());
        log.debug("成功从Authorization头中提取令牌");
        return token;
    }

    /**
     * 验证令牌是否有效
     * @param token JWT令牌
     * @return 如果令牌有效返回true，否则返回false
     */
    public boolean validateToken(String token) {
        log.debug("开始验证令牌");
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            log.debug("令牌验证成功");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("令牌已过期");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("令牌无效: {}", e.getMessage());
            return false;
        }
    }
}