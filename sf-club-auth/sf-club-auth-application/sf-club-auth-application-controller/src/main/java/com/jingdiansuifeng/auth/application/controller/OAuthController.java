package com.jingdiansuifeng.auth.application.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.google.common.base.Preconditions;
import com.jingdiansuifeng.auth.domain.entity.OAuthLoginBO;
import com.jingdiansuifeng.auth.domain.entity.OAuthRenderBO;
import com.jingdiansuifeng.auth.domain.service.OAuthDomainService;
import com.jingdiansuifeng.auth.entity.OAuthLoginReqDTO;
import com.jingdiansuifeng.auth.entity.OAuthRenderVO;
import com.jingdiansuifeng.auth.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 统一第三方 OAuth 认证控制器 (OAuthController)
 */
@RestController
@RequestMapping("/oauth/")
@Slf4j
public class OAuthController {

    @Resource
    private OAuthDomainService oauthDomainService;

    /**
     * 获取指定第三方平台的授权跳转 URL 与防伪 State
     *
     * @param platform 平台标识: FEISHU, GITHUB, ZHIHU (大小写不敏感)
     */
    @GetMapping("render/{platform}")
    public Result<OAuthRenderVO> renderAuthorize(@PathVariable("platform") String platform) {
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(platform), "平台参数不能为空");
            OAuthRenderBO bo = oauthDomainService.renderAuthorize(platform);
            OAuthRenderVO vo = OAuthRenderVO.builder()
                    .platform(bo.getPlatform())
                    .authorizeUrl(bo.getAuthorizeUrl())
                    .state(bo.getState())
                    .build();
            return Result.ok(vo);
        } catch (IllegalArgumentException e) {
            log.warn("OAuthController.renderAuthorize invalid param: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("OAuthController.renderAuthorize error: {}", e.getMessage(), e);
            return Result.fail("获取授权链接失败: " + e.getMessage());
        }
    }

    /**
     * 统一第三方授权回调登录接口
     * 前端获取 Code 和 State 后调用此接口换取 Sa-Token 登录凭据
     */
    @PostMapping("login")
    public Result<SaTokenInfo> doOAuthLogin(@RequestBody OAuthLoginReqDTO reqDTO) {
        try {
            Preconditions.checkNotNull(reqDTO, "入参不能为空");
            Preconditions.checkArgument(StringUtils.isNotBlank(reqDTO.getPlatform()), "平台类型不能为空");
            Preconditions.checkArgument(StringUtils.isNotBlank(reqDTO.getCode()), "授权 Code 不能为空");

            OAuthLoginBO loginBO = OAuthLoginBO.builder()
                    .platform(reqDTO.getPlatform())
                    .code(reqDTO.getCode())
                    .state(reqDTO.getState())
                    .build();

            SaTokenInfo tokenInfo = oauthDomainService.doOAuthLogin(loginBO);
            return Result.ok(tokenInfo);
        } catch (IllegalArgumentException e) {
            log.warn("OAuthController.doOAuthLogin invalid param: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("OAuthController.doOAuthLogin error: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

}
