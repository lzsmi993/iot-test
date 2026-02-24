package com.iot.platform.controller;

import com.iot.platform.common.PageResult;
import com.iot.platform.common.Result;
import com.iot.platform.dto.LoginLogQueryDTO;
import com.iot.platform.service.LoginLogService;
import com.iot.platform.vo.LoginLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 登录日志 Controller
 */
@RestController
@RequestMapping("/api/v1/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    /**
     * 分页查询登录日志
     *
     * @param queryDTO 查询参数（支持 username、ip、startTime、endTime、result、eventType、loginMethod）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<LoginLogVO>> page(LoginLogQueryDTO queryDTO) {
        PageResult<LoginLogVO> pageResult = loginLogService.queryPage(queryDTO);
        return Result.ok(pageResult);
    }

    /**
     * 查询登录日志详情
     *
     * @param id 日志 ID
     * @return 日志详情
     */
    @GetMapping("/{id}")
    public Result<LoginLogVO> detail(@PathVariable Long id) {
        LoginLogVO vo = loginLogService.getDetailById(id);
        if (vo == null) {
            return Result.fail(404, "记录不存在");
        }
        return Result.ok(vo);
    }

    /**
     * 导出登录日志（CSV，上限 10000 条）
     *
     * @param queryDTO 查询参数
     * @param response HTTP 响应
     */
    @GetMapping("/export")
    public void export(LoginLogQueryDTO queryDTO, HttpServletResponse response) {
        loginLogService.exportCsv(queryDTO, response);
    }
}
