package com.coffee.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {
    private String secret = "cafe";

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    // Trích xuất ngày hết hạn
    public Date extractExpiration(String token){
        return extractClaims(token, Claims::getExpiration);
    }

    // Xác nhận quyền sở hữu
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Trích xuất tất cả quyền sở hữu
    // Ai đó xử lý mã thông báo jwt thì khóa bí mật sẽ tự động được thay đổi ngay lập tức
    public Claims extractAllClaims(String token){
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // Kiểm tra xem thời gian gửi mã thông báo đã hết hạn chưa
    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    // Giúp trả về chuỗi giá trị
    public String generateToken(String username,String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }
    private String createToken(Map<String, Object> claims, String subject){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))// mã sẽ hết hạn sau 10h
                .signWith(SignatureAlgorithm.HS256,secret).compact();// chữ kí mã hóa
    }
    // Xác thực mã thông báo
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
