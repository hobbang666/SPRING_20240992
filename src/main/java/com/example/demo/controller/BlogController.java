package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;
import com.example.demo.model.domain.Board; // Board 도메인 추가
import com.example.demo.model.domain.Article;
import com.example.demo.model.service.BlogService; // 최상단 서비스 클래스 연동 추가
import com.example.demo.model.service.AddArticleRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jakarta.servlet.http.HttpSession;

@Controller // 컨트롤러 어노테이션 명시
public class BlogController {
    // 클래스 하단 작성
    @Autowired
    BlogService blogService; // DemoController 클래스 아래 객체 생성

    @GetMapping("/article_list") // 게시판 링크 지정
    public String article_list(Model model) {
        List<Article> list = blogService.findAll(); // 게시판 리스트
        model.addAttribute("articles", list); // 모델에 추가
        return "article_list"; // .HTML 연결
    }

    // @GetMapping("/board_list") // 새로운 게시판 링크 지정
    // public String board_list(Model model) {
    // List<Board> list = blogService.findAllBoard(); // 게시판 전체 리스트, 기존 Article에서
    // Board로 변경됨
    // model.addAttribute("boards", list); // 모델에 추가
    // return "board_list"; // .HTML 연결
    // }

    @GetMapping("/board_list") // 새로운 게시판 링크 지정
    public String board_list(Model model, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String keyword, jakarta.servlet.http.HttpSession session) {

        // 👈 2. 세션 값 가져오기 및 3. 로그인 체크 로직 추가
        String userId = (String) session.getAttribute("userId");
        String email = (String) session.getAttribute("email");

        if (userId == null) {
            // 로그인하지 않은 경우 로그인 페이지로 리다이렉트 (필수)
            return "redirect:/member_login";
        }

        // 👈 4. 모델에 email 값을 추가합니다. (화면 출력용)
        model.addAttribute("email", email);
        int pageSize = 3;
        PageRequest pageable = PageRequest.of(page, pageSize); // 한 페이지의 게시글 수
        Page<Board> list; // Page를 반환
        if (keyword.isEmpty()) {
            list = blogService.findAll(pageable); // 기본 전체 출력(키워드 x)
        } else {
            list = blogService.searchByKeyword(keyword, pageable); // 키워드로 검색
        }
        int startNum = (page * pageSize) + 1;
        model.addAttribute("startNum", startNum);
        model.addAttribute("boards", list); // 모델에 추가
        model.addAttribute("totalPages", list.getTotalPages()); // 페이지 크기
        model.addAttribute("currentPage", page); // 페이지 번호
        model.addAttribute("keyword", keyword); // 키워드
        return "board_list"; // .HTML 연결
    }

    @GetMapping("/board_view/{id}") // 게시판 링크 지정
    public String board_view(Model model, @PathVariable Long id, jakarta.servlet.http.HttpSession session) {

        String email = (String) session.getAttribute("email");

        if (email == null) {
            return "redirect:/member_login";
        }
        model.addAttribute("email", email);

        Optional<Board> list = blogService.findByIdBoard(id); // 선택한 게시판 글
        if (list.isPresent()) {
            // 기존 코드에서는 단일 Board 객체를 "boards"라는 이름으로 전달하고 있음
            model.addAttribute("boards", list.get());
        } else {
            return "/error_page/article_error";
        }
        return "board_view"; // .HTML 연결
    }

    @PostMapping("/articles")
    public String addArticle(@ModelAttribute AddArticleRequest request) {
        blogService.save(request); // 글 저장
        return "redirect:/article_list"; // 저장 후 목록으로 리다이렉트
    }

    @GetMapping("/article_edit/{id}") // 게시판 링크 지정
    public String article_edit(Model model, @PathVariable Long id) {
        Optional<Article> list = blogService.findById(id); // 선택한 게시판 글
        if (list.isPresent()) {
            model.addAttribute("article", list.get()); // 존재하면 Article 객체를 모델에 추가
        } else {
            // 처리할 로직 추가 (예: 오류 페이지로 리다이렉트, 예외 처리 등)
            return "error_page/article_error"; // 오류 처리 페이지로 연결(이름 수정됨)
        }
        return "article_edit"; // .HTML 연결
    }

    @PutMapping("/api/article_edit/{id}")
    public String updateArticle(@PathVariable Long id, @ModelAttribute AddArticleRequest request) {
        blogService.update(id, request);
        return "redirect:/article_list"; // 글 수정 이후 .html 연결
    }

    @DeleteMapping("/api/article_delete/{id}")
    public String deleteArticle(@PathVariable Long id) {
        blogService.delete(id);
        return "redirect:/article_list";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "/error_page/article_error2";
    }

    @GetMapping("/board_edit/{id}")
    public String board_edit(Model model, @PathVariable Long id) {
        Optional<Board> board = blogService.findByIdBoard(id);
        if (board.isPresent()) {
            model.addAttribute("board", board.get());
        } else {
            return "error_page/article_error";
        }
        return "board_edit";
    }

    @PutMapping("/api/board_edit/{id}")
    public String updateBoard(@PathVariable Long id, @ModelAttribute Board request) {
        blogService.updateBoard(id, request);
        return "redirect:/board_list";
    }

    @GetMapping("/board_write")
    public String board_write() {
        return "board_write";
    }

    @PostMapping("/api/boards")
    public String addboards(@ModelAttribute AddArticleRequest request,
            jakarta.servlet.http.HttpSession session) {

        String email = (String) session.getAttribute("email");

        if (email == null) {
            return "redirect:/member_login";
        }

        // 1. user/email 필드 설정
        request.setUser(email);
        request.setEmail(email);

        // 2. address 필드에 기본값 설정
        if (request.getAddress() == null || request.getAddress().isEmpty()) {
            request.setAddress("미등록 주소");
        }

        // 3. age 필드에 기본값 설정
        request.setAge(30L);

        // 4. mobile/name 필드에 기본값 설정
        request.setMobile("000-0000-0000");
        request.setName("익명 작성자");

        // 🌟 5. password 필드에 기본값 설정 🌟 (마지막 필수 필드)
        request.setPassword("dummy_password_1234");

        blogService.save(request);
        return "redirect:/board_list";
    }

    @DeleteMapping("/api/board_delete/{id}")
    public String deleteBoard(@PathVariable Long id) {
        blogService.deleteBoard(id);
        return "redirect:/board_list";
    }

}