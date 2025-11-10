package com.hackathon.tomolow.domain.group.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hackathon.tomolow.domain.group.dto.GroupCreateDto;
import com.hackathon.tomolow.domain.group.dto.GroupSearchResponseDto;
import com.hackathon.tomolow.domain.group.service.GroupEnterService;
import com.hackathon.tomolow.domain.group.service.GroupService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/group")
@Tag(name = "Group", description = "그룹 관련 API")
public class GroupController {

  private final GroupService groupService;
  private final GroupEnterService groupEnterService;

  @PostMapping
  public ResponseEntity<BaseResponse<?>> createGroup(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody GroupCreateDto groupCreateDto) {
    Long userId = customUserDetails.getUser().getId();
    Long savedGroupId = groupService.createGroup(userId, groupCreateDto);
    return ResponseEntity.ok(BaseResponse.success(savedGroupId));
  }

  @GetMapping("/join")
  public ResponseEntity<BaseResponse<?>> searchGroup(@RequestParam @NotBlank String code) {
    GroupSearchResponseDto groupSearchResponseDto = groupEnterService.searchGroup(code);
    return ResponseEntity.ok(BaseResponse.success(groupSearchResponseDto));
  }

  @PostMapping("/join/{groupId}")
  public ResponseEntity<BaseResponse<?>> joinGroup(
      @PathVariable Long groupId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    Long joinedGroupId = groupEnterService.joinGroup(userId, groupId);
    return ResponseEntity.ok(BaseResponse.success(joinedGroupId));
  }
}
