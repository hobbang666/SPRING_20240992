package com.example.demo.model.domain;

import lombok.*; // 어노테이션 자동 생성
import jakarta.persistence.*; // 기존 javax 후속 버전

@Getter // setter는 없음(무분별한 변경 x)
@Entity // 아래 객체와 DB 테이블을 매핑. JPA가 관리
@Table(name = "board") // 테이블 이름을 지정. 없는 경우 클래스이름으로 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부 생성자 접근 방지
@Builder

public class Board {
    @Id // 기본 키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 1씩 증가
    @Column(name = "id", updatable = false) // 수정 x
    private Long id;
    @Column(name = "title", nullable = false) // null x
    private String title = "";
    @Column(name = "content", nullable = false)
    private String content = "";
    @Column(name = "user", nullable = false) // 이름
    private String user = "";
    @Column(name = "newdate", nullable = false) // 날짜
    private String newdate = "";
    @Column(name = "count", nullable = false) // 조회수
    private Long count;
    @Column(name = "likec", nullable = false) // 좋아요
    private Long likec;
    @Column(name = "address")
    private String address = "";
    @Column(name = "age", nullable = false) // DB 제약 조건에 맞춰 추가 (NOT NULL)
    private Long age;
    @Column(name = "mobile")
    private String mobile;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "password") // 👈 추가
    private String password;

    @Builder // 생성자에 빌더 패턴 적용(불변성)
    public Board(String title, String content, String user, String newdate, Long count, Long likec, String address,
            Long age, String mobile, String name, String email, String password) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.newdate = newdate;
        this.count = count;
        this.likec = likec;
        this.address = address;
        this.age = age;
        this.mobile = mobile; // 👈 추가
        this.name = name; // 👈 추가
        this.email = email; // 👈 추가
        this.password = password; // 👈 추가
    }

    public void update(String title, String content, String user, String newdate, Long count, Long likec, Long age,
            String address, String mobile, String name, String email, String password) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.newdate = newdate;
        this.count = count;
        this.likec = likec;
        this.age = age;
        this.address = address;
        this.mobile = mobile; // 👈 추가
        this.name = name; // 👈 추가
        this.email = email; // 👈 추가
        this.password = password; // 👈 추가
    }
}