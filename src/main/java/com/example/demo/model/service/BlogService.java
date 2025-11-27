package com.example.demo.model.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.model.domain.Article;
import com.example.demo.model.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import com.example.demo.model.repository.BoardRepository;
import com.example.demo.model.domain.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor // 생성자 자동 생성(부분)
public class BlogService {
    @Autowired // 객체 주입 자동화, 생성자 1개면 생략 가능
    private final BlogRepository blogRepository; // 리포지토리 선언
    private final BoardRepository boardRepository; // 리포지토리 선언

    public List<Article> findAll() { // 게시판 전체 목록 조회
        return blogRepository.findAll();
    }

    public List<Board> findAllBoard() { // 게시판 전체 목록 조회
        return boardRepository.findAll();
    }

    public Board save(AddArticleRequest request) {
        // DTO가 없는 경우 이곳에 직접 구현 가능
        return boardRepository.save(request.toEntity());
    }

    public Optional<Article> findById(Long id) { // 게시판 특정 글 조회
        return blogRepository.findById(id);
    }

    public Optional<Board> findByIdBoard(Long id) { // 게시판 특정 글 조회
        return boardRepository.findById(id);
    }

    public void update(Long id, AddArticleRequest request) {
        Optional<Article> optionalArticle = blogRepository.findById(id);
        optionalArticle.ifPresent(article -> {
            article.update(
                    request.getTitle(),
                    request.getContent());
            blogRepository.save(article);
        });
    }

    public void delete(Long id) {
        blogRepository.deleteById(id);
    }

    public void updateBoard(Long id, Board request) {
        // 원본 게시글을 DB에서 불러옵니다.
        Optional<Board> optionalBoard = boardRepository.findById(id);

        optionalBoard.ifPresent(originalBoard -> {

            // 1. request에서 받은 title과 content를 사용합니다.
            // 만약 title/content가 null이면 (수정 폼에 없었다면) 원본 값을 사용합니다.
            String updatedTitle = request.getTitle() != null ? request.getTitle() : originalBoard.getTitle();
            String updatedContent = request.getContent() != null ? request.getContent() : originalBoard.getContent();

            // 2. originalBoard.update() 호출 시,
            // 수정되는 필드(title, content)를 제외한 모든 필수 필드는
            // 원본(originalBoard)에서 가져와 재주입합니다.
            originalBoard.update(
                    updatedTitle,
                    updatedContent,
                    originalBoard.getUser(), // 원본 값 유지
                    originalBoard.getNewdate(), // 원본 값 유지
                    originalBoard.getCount(), // 원본 값 유지
                    originalBoard.getLikec(), // 원본 값 유지
                    originalBoard.getAge(), // 원본 값 유지
                    originalBoard.getAddress(), // 원본 값 유지 (오류 해결)
                    originalBoard.getMobile(), // 원본 값 유지
                    originalBoard.getName(), // 원본 값 유지
                    originalBoard.getEmail(), // 원본 값 유지
                    originalBoard.getPassword() // 원본 값 유지
            );

            boardRepository.save(originalBoard);
        });
    }

    public Page<Board> findAll(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

    public Page<Board> searchByKeyword(String keyword, Pageable pageable) {
        return boardRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    } // LIKE 검색 제공(대소문자 무시)

    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

}
