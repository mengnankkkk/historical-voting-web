package com.historical.voting.user.entity;

import javax.persistence.*;

import com.historical.voting.user.entity.type.UserRank;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String nickname;
    private LocalDate birthday;
    private String avatar;
    private String phoneNumber;

    @Column(name = "auth_provider")
    private String authProvider = "GITHUB";

    private String providerId;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(nullable = false)
    private Integer experience = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_rank", nullable = false)
    private UserRank userRank = UserRank.NEWCOMER;

    private Integer votingCount = 0;
    private Integer commentCount = 0;
    private Integer likedCount = 0;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    private Boolean isEnabled = false;
    private Boolean isLocked = false;
} 