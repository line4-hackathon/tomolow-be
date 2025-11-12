package com.hackathon.tomolow.domain.userGroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.userGroup.dto.UserGroupDetailResponseDto;
import com.hackathon.tomolow.domain.userGroup.service.UserGroupDetailService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/group")
@Tag(name = "Group Detail", description = "개별 그룹 조회 API")
public class UserGroupController {

  private final UserGroupDetailService userGroupDetailService;

  @GetMapping("/{groupId}")
  public ResponseEntity<BaseResponse<?>> getUserGroupDetail(
      @PathVariable("groupId") Long groupId,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    UserGroupDetailResponseDto userGroupDetails =
        userGroupDetailService.getUserGroupDetails(userId, groupId);
    return ResponseEntity.ok(BaseResponse.success(userGroupDetails));
  }
}
