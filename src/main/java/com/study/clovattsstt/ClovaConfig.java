package com.study.clovattsstt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@NoArgsConstructor
public class ClovaConfig {
    // Clova Speech secret key -> 빌더에서 확인 가능
    @Value("${SECRET}")
    private String SECRET;
    // Clova Speech invoke URL -> 빌더에서 확인 가능
    @Value("${INVOKE_URL}")
    private String INVOKE_URL;
}
