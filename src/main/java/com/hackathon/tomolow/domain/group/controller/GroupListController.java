package com.hackathon.tomolow.domain.group.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.group.dto.GroupListResponseDto;
import com.hackathon.tomolow.domain.group.dto.JoinableGroupListResponseDto;
import com.hackathon.tomolow.domain.group.service.GroupListService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/group")
@Tag(name = "GroupList", description = "그룹 리스트 조회 API")
public class GroupListController {

  private final GroupListService groupListService;

  @GetMapping
  public ResponseEntity<BaseResponse<?>> getActiveAndExpiredGroupList(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    GroupListResponseDto activeGroupList = groupListService.getActiveAndExpiredGroupList(userId);
    return ResponseEntity.ok(BaseResponse.success(activeGroupList));
  }

  @GetMapping("/joinable")
  public ResponseEntity<BaseResponse<?>> getJoinableGroupList(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    JoinableGroupListResponseDto joinableGroupList = groupListService.getJoinableGroupList(userId);
    return ResponseEntity.ok(BaseResponse.success(joinableGroupList));
  }
}
