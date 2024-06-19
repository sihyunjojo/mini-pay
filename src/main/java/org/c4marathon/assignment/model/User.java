package org.c4marathon.assignment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "users")
// 한 사람이 여러 아이디를 만들 필요가 있는가??
// 없다.
// 그럼 사람 한명이 하나만 가지고 있는 고유한 무엇인가가 필요하다.
// 주민등록번호를 받을까??
// 닉네임을 써보자
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실제 이름
    @Column(nullable = false)
    private String name;

    // 처음에는 email과 name을 조합하여서 default값으로 만들어줌.
    // 추후 닉네임 변경시, 로그인을 다시 해야함!?!?
    @Column(nullable = false, unique = true)
    private String nickname;

    // 로그인 시, 사용할 것들
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    // 입력시 json 으로 들어오는 값을 무시하는 것?
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

    public User() {
        this.accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

    @Override
    public String toString() {
        StringBuilder accountsStr = new StringBuilder();
        for (Account account : accounts) {
            accountsStr.append(account.toString()).append(", ");
        }

        // Remove the last comma and space if there are accounts
        if (!accountsStr.isEmpty()) {
            accountsStr.setLength(accountsStr.length() - 2);
        }

        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", accounts=[" + accountsStr + "]" +
                '}';
    }
}