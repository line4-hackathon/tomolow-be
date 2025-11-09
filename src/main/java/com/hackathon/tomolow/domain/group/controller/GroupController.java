package com.hackathon.tomolow.domain.group.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.group.dto.GroupCreateDto;
import com.hackathon.tomolow.domain.group.service.GroupService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group")
@Tag(name = "Group", description = "그룹 관련 API")
public class GroupController {

  private final GroupService groupService;

  @PostMapping
  public ResponseEntity<BaseResponse<?>> createGroup(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody GroupCreateDto groupCreateDto) {
    Long userId = customUserDetails.getUser().getId();
    Long savedGroupId = groupService.createGroup(userId, groupCreateDto);
    return ResponseEntity.ok(BaseResponse.success(savedGroupId));
  }
}
