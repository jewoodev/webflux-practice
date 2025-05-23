package com.heri2go.chat.web.service.auth;

import com.heri2go.chat.domain.token.RefreshHash;
import com.heri2go.chat.domain.token.RefreshHashRepository;
import com.heri2go.chat.web.controller.auth.request.RefreshRequest;
import com.heri2go.chat.web.exception.RefreshFailedException;
import com.heri2go.chat.web.service.auth.response.RefreshResponse;
import com.heri2go.chat.web.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class RefreshHashService {

    private final RefreshHashRepository refreshHashRepository;
    private final UserService userService;
    private final JwtService jwtService;

    public Mono<RefreshHash> save(RefreshHash refreshHash) {
        return refreshHashRepository.save(refreshHash);
    }

    /**
     * [토큰을 리프레쉬하는 서비스]
     * Redis에 저장된 RefreshToken을 갱신하고 AccessToken과 RefreshToken을 갱신해 리턴한다.
     */
    public Mono<RefreshResponse> refresh(RefreshRequest refreshRequest) {
        return validateRefreshRequest(refreshRequest)
                .flatMap(refreshHash -> renewRefreshToken(refreshRequest))
                .flatMap(refreshHash -> renewAccessToken(refreshRequest, refreshHash));
    }

    private Mono<String> validateRefreshRequest(RefreshRequest refreshRequest) {
        return refreshHashRepository.findByUsername(refreshRequest.username())
                .switchIfEmpty(Mono.error(new RefreshFailedException("유효하지 않은 리프레시 요청입니다(username).")))
                .filter(refreshToken -> refreshToken.equals(refreshRequest.refreshToken()))
                .switchIfEmpty(Mono.error(new RefreshFailedException("유효하지 않은 리프레시 요청입니다(refreshToken).")));
    }

    private Mono<RefreshHash> renewRefreshToken(RefreshRequest refreshRequest) {
        String newRefreshToken = jwtService.generateRefreshToken(refreshRequest.username());
        return refreshHashRepository.save(
                RefreshHash.builder()
                        .username(refreshRequest.username())
                        .refreshToken(newRefreshToken)
                        .build()
        );
    }

    private Mono<RefreshResponse> renewAccessToken(RefreshRequest refreshRequest, RefreshHash refreshHash) {
        return userService.getByUsername(refreshRequest.username())
                .map(userResponse -> jwtService.generateAccessToken(userResponse.username()))
                .map(newAccessToken -> RefreshResponse.from(newAccessToken, refreshHash.refreshToken()));
    }
}
