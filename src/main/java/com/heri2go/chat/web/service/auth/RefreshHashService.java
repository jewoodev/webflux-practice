package com.heri2go.chat.web.service.auth;

import com.heri2go.chat.domain.token.RefreshHash;
import com.heri2go.chat.domain.token.RefreshHashRepository;
import com.heri2go.chat.web.controller.auth.request.RefreshRequest;
import com.heri2go.chat.web.exception.RefreshFailedException;
import com.heri2go.chat.web.service.auth.response.RefreshResponse;
import com.heri2go.chat.web.service.user.UserService;
import com.heri2go.chat.web.service.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshHashService {

    private final RefreshHashRepository refreshHashRepository;
    private final UserService userService;
    private final JwtService jwtService;

    public RefreshHash save(RefreshHash refreshHash) {
        return refreshHashRepository.save(refreshHash);
    }

    public RefreshResponse refresh(RefreshRequest refreshRequest) {
        validateRefreshRequest(refreshRequest);
        RefreshHash refreshHash = renewRefreshToken(refreshRequest);
        return renewAccessToken(refreshRequest, refreshHash);
    }

    private void validateRefreshRequest(RefreshRequest refreshRequest) {
        String storedToken = refreshHashRepository.findByUsername(refreshRequest.username());
        if (storedToken == null) {
            throw new RefreshFailedException("유효하지 않은 리프레시 요청입니다(username).");
        }
        if (!storedToken.equals(refreshRequest.refreshToken())) {
            throw new RefreshFailedException("유효하지 않은 리프레시 요청입니다(refreshToken).");
        }
    }

    private RefreshHash renewRefreshToken(RefreshRequest refreshRequest) {
        String newRefreshToken = jwtService.generateRefreshToken(refreshRequest.username());
        return refreshHashRepository.save(
                RefreshHash.builder()
                        .username(refreshRequest.username())
                        .refreshToken(newRefreshToken)
                        .build()
        );
    }

    private RefreshResponse renewAccessToken(RefreshRequest refreshRequest, RefreshHash refreshHash) {
        UserResponse userResponse = userService.getByUsername(refreshRequest.username());
        String newAccessToken = jwtService.generateAccessToken(userResponse.username());
        return RefreshResponse.from(newAccessToken, refreshHash.refreshToken());
    }
}
