package com.quoraBackend.controllers;

import com.quoraBackend.dto.LikeRequestDTO;
import com.quoraBackend.dto.LikeResponseDTO;
import com.quoraBackend.services.ILikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final ILikeService likeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LikeResponseDTO> createLike(@Valid @RequestBody LikeRequestDTO likeRequestDTO) {
        return likeService.createLike(likeRequestDTO);
    }

    @PostMapping("/toggle")
    @ResponseStatus(HttpStatus.OK)
    public Mono<LikeResponseDTO> toggleLike(
            @RequestParam String userId,
            @RequestParam String targetId,
            @RequestParam String targetType,
            @RequestParam Boolean liked) {
        return likeService.toggleLike(userId, targetId, targetType, liked);
    }

    @GetMapping("/count/likes")
    @ResponseStatus(HttpStatus.OK)
    public Mono<LikeResponseDTO> getLikesCount(
            @RequestParam String targetId,
            @RequestParam String targetType) {
        return likeService.countLikesByTargetIdAndTargetType(targetId, targetType);
    }

    @GetMapping("/count/dislikes")
    @ResponseStatus(HttpStatus.OK)
    public Mono<LikeResponseDTO> getDislikesCount(
            @RequestParam String targetId,
            @RequestParam String targetType) {
        return likeService.countDisLikesByTargetIdAndTargetType(targetId, targetType);
    }
}