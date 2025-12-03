package com.example.demo.controller;

import java.util.List;
import java.io.File; // 파일 처리 import
import java.io.IOException; // 입출력 예외 import
import java.util.UUID; // 고유 ID 생성 import
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
import org.springframework.web.multipart.MultipartFile; // 파일 처리 import
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // 리다이렉트 속성 import

@Controller // 컨트롤러 어노테이션 명시
public class BlogController {
    // 클래스 하단 작성
    @Autowired
    BlogService blogService; // DemoController 클래스 아래 객체 생성

    // 파일 저장 경로 (실제 환경에 맞게 변경 필요. 반드시 존재하는 폴더여야 합니다.)
    private final String UPLOAD_DIR = "C:/uploads/";

    @GetMapping("/article_list") // 게시판 링크 지정
    public String article_list(Model model) {
        List<Article> list = blogService.findAll(); // 게시판 리스트
        model.addAttribute("articles", list); // 모델에 추가
        return "article_list"; // .HTML 연결
    }

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

    // 🚨 파일 업로드 로직이 통합된 게시글 추가 메서드 🚨
    @PostMapping("/api/boards")
    public String addboards(
            @ModelAttribute AddArticleRequest request,
            @RequestParam("files") List<MultipartFile> files, // HTML의 name="files"를 받음
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes) { // 에러 메시지 전달용 추가

        String email = (String) session.getAttribute("email");
        Long newBoardId = 0L; // 게시글 ID를 저장할 변수 초기화 (DB 저장 후 실제 ID로 변경 필요)

        if (email == null) {
            return "redirect:/member_login";
        }

        try {
            // 1. 텍스트 데이터 처리 (기존 로직)
            request.setUser(email);
            request.setEmail(email);
            if (request.getAddress() == null || request.getAddress().isEmpty()) {
                request.setAddress("미등록 주소");
            }
            request.setAge(30L);
            request.setMobile("000-0000-0000");
            request.setName("익명 작성자");
            request.setPassword("dummy_password_1234");

            // 2. 게시글 저장 (여기서 newBoardId를 실제 DB ID로 업데이트해야 함)
            blogService.save(request); // 이 메서드가 Board 객체를 DB에 저장하고 ID를 반환해야 합니다.
            // newBoardId = blogService.saveAndGetId(request); // 실제 구현 시 이렇게 변경되어야 함
            newBoardId = 1L; // 임시 ID 사용

            // 3. 파일 업로드 로직 (2개 파일 처리, 이름 충돌 방지, 에러 처리)
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue; // 파일이 선택되지 않았으면 건너뜀
                }

                // 3-1. 파일 크기 초과 에러 처리 (5MB 제한 예시)
                if (file.getSize() > 5 * 1024 * 1024) {
                    throw new FileUploadException("파일 크기가 5MB를 초과했습니다: " + file.getOriginalFilename());
                }

                // 3-2. 동일 파일 업로드 시 다른 이름으로 저장 (UUID 사용)
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // 3-3. 파일 저장
                File targetFile = new File(UPLOAD_DIR, uniqueFileName);
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs(); // 폴더가 없으면 생성
                }
                file.transferTo(targetFile);

                // 3-4. DB에 파일 정보 저장 (글 ID, 원본 파일명, 저장된 파일명 등을 저장하는 서비스 로직 필요)
                // fileService.saveFileInfo(newBoardId, originalFilename, uniqueFileName,
                // targetFile.getAbsolutePath());
            }

        } catch (FileUploadException e) {
            // 파일 업로드 관련 오류 처리
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/uploadError"; // 에러 페이지로 리다이렉트

        } catch (IOException e) {
            // 파일 저장 중 입출력 오류 처리
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "파일 저장 중 시스템 오류가 발생했습니다.");
            return "redirect:/uploadError";

        } catch (Exception e) {
            // 기타 게시글 처리 중 발생한 오류
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 작성 중 알 수 없는 오류가 발생했습니다.");
            return "redirect:/uploadError";
        }

        // 4. 성공 시 게시글 목록으로 리다이렉트
        return "redirect:/board_list";
    }

    @DeleteMapping("/api/board_delete/{id}")
    public String deleteBoard(@PathVariable Long id) {
        blogService.deleteBoard(id);
        return "redirect:/board_list";
    }

    // 🌟🌟🌟 새로운 GET 매핑 추가: 파일 업로드 에러 페이지 핸들러 🌟🌟🌟
    @GetMapping("/uploadError")
    public String handleError(Model model, @ModelAttribute("errorMessage") String errorMessage) {
        // 리다이렉트 시 전달된 errorMessage가 없으면 기본 메시지 설정
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "잘못된 파일 접근 또는 처리 오류입니다.";
        }

        model.addAttribute("message", errorMessage);
        // "uploadErrorPage"라는 뷰(HTML 파일)를 찾습니다.
        return "uploadErrorPage";
    }
    // 🌟🌟🌟 ------------------------------------------------ 🌟🌟🌟

    // 파일 업로드 전용 예외 클래스 (컨트롤러 내부에 정의)
    private static class FileUploadException extends Exception {
        public FileUploadException(String message) {
            super(message);
        }
    }
}