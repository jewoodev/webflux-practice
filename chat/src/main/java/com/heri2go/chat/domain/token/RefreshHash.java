package com.heri2go.chat.domain.token;

import lombok.Builder;

@Builder
public record RefreshHash (
        String username, // 레디스 키 생성에 사용할 값, 레디스 값으로는 저장 x
        String refreshToken
){

}
