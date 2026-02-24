package com.jpa.market.entity;

import com.jpa.market.constant.OAuthType;
import com.jpa.market.constant.Role;
import com.jpa.market.dto.MemberJoinDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@ToString
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loginId;

    private String password;

    private String name;

    @Column(unique = true)
    private String email;

    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private OAuthType oauthType;

    public static Member createMember(MemberJoinDto dto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.loginId = dto.getLoginId();
        member.password = passwordEncoder.encode(dto.getPassword());
        member.name = dto.getName();
        member.email = dto.getEmail();
        member.address = dto.getAddress();
        member.role = Role.USER;
        member.oauthType = OAuthType.SHOP;

        return member;
    }

    // 소셜로그인용
    public static Member createOAuthMember(String loginId, String nickname, String email, String password, OAuthType oauthType) {
        Member member = new Member();
        member.loginId = loginId;
        member.password = password;
        member.name = nickname;
        member.email = email;
        member.role = Role.USER;
        member.oauthType = oauthType;
        return member;
    }
}
