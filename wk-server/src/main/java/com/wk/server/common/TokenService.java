package com.wk.server.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 令牌服务：HMAC-SHA256 签名的无状态令牌，格式 base64url(payload).base64url(signature)。
 * payload = 用户名:过期毫秒时间戳，有效期默认 1 天（application.properties 的 app.auth.ttl-ms）。
 */
@Service
public class TokenService {

	private final String secret;
	private final long ttlMs;

	public TokenService(@Value("${app.auth.secret}") String secret, @Value("${app.auth.ttl-ms}") long ttlMs) {
		this.secret = secret;
		this.ttlMs = ttlMs;
	}

	public record TokenInfo(String token, long expiresAt) {
	}

	public TokenInfo create(String username) {
		long expiresAt = System.currentTimeMillis() + ttlMs;
		String payloadB64 = base64Url((username + ":" + expiresAt).getBytes(StandardCharsets.UTF_8));
		return new TokenInfo(payloadB64 + "." + sign(payloadB64), expiresAt);
	}

	// 校验通过返回用户名，签名不符或过期返回 null
	public String verify(String token) {
		if (token == null || token.isBlank()) {
			return null;
		}
		int dot = token.lastIndexOf('.');
		if (dot <= 0) {
			return null;
		}
		String payloadB64 = token.substring(0, dot);
		String signature = token.substring(dot + 1);
		// 常量时间比较，防时序攻击
		if (!MessageDigest.isEqual(sign(payloadB64).getBytes(StandardCharsets.UTF_8),
				signature.getBytes(StandardCharsets.UTF_8))) {
			return null;
		}
		try {
			String payload = new String(Base64.getUrlDecoder().decode(payloadB64), StandardCharsets.UTF_8);
			int sep = payload.lastIndexOf(':');
			long expiresAt = Long.parseLong(payload.substring(sep + 1));
			if (System.currentTimeMillis() > expiresAt) {
				return null;
			}
			return payload.substring(0, sep);
		} catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
			return null;
		}
	}

	private String sign(String payloadB64) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return base64Url(mac.doFinal(payloadB64.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("令牌签名失败", e);
		}
	}

	private String base64Url(byte[] data) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
	}

}
