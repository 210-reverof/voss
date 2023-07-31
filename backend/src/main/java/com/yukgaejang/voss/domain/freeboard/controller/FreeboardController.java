package com.yukgaejang.voss.domain.freeboard.controller;

import com.yukgaejang.voss.domain.freeboard.service.PostCommentService;
import com.yukgaejang.voss.domain.freeboard.service.PostService;
import com.yukgaejang.voss.domain.freeboard.service.dto.request.CreateCommentRequest;
import com.yukgaejang.voss.domain.freeboard.service.dto.request.CreatePostRequest;
import com.yukgaejang.voss.domain.freeboard.service.dto.request.UpdateCommentRequest;
import com.yukgaejang.voss.domain.freeboard.service.dto.request.UpdatePostRequest;
import com.yukgaejang.voss.domain.freeboard.service.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/freeboard")
public class FreeboardController {
    private final PostService postService;
    private final PostCommentService postCommentService;

    @PostMapping
    public ResponseEntity<CreatePostResponse> createPost(@RequestBody CreatePostRequest createPostRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(postService.createPost(email, createPostRequest));
    }

    @PutMapping("/{postId}")
    public  ResponseEntity<UpdatePostResponse> updatePost(@PathVariable Long postId, @RequestBody UpdatePostRequest updatePostRequest) {
        return ResponseEntity.ok(postService.updatePost(postId, updatePostRequest));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostDetail(postId));
    }

    @GetMapping
    public ResponseEntity<Page<PostListResponse>> getPostList(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(postService.getPostList(pageable));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<DeletePostResponse> deletePost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.deletePost(postId));
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<CreateCommentResponse> createComment(@PathVariable Long postId, @RequestBody CreateCommentRequest createCommentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(postCommentService.createComment(postId, email, createCommentRequest));
    }

    @GetMapping("/{postId}/comment")
    public ResponseEntity<Page<CommentDetailResponse>> getComments(@PathVariable Long postId, @PageableDefault(size = 100) Pageable pageable) {
        return ResponseEntity.ok(postCommentService.getComments(postId, pageable));
    }

    @PutMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<UpdateCommentResponse> updateComment(@PathVariable Long commentId, @RequestBody UpdateCommentRequest updateCommentRequest) {
        return ResponseEntity.ok(postCommentService.updateComment(commentId, updateCommentRequest));
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<DeleteCommentResponse> deleteComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(postCommentService.deleteComment(commentId));
    }
}