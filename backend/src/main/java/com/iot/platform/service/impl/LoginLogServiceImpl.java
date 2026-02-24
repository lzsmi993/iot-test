package com.iot.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iot.platform.common.PageResult;
import com.iot.platform.common.SecurityContextHolder;
import com.iot.platform.dto.LoginLogQueryDTO;
import com.iot.platform.entity.LoginLog;
import com.iot.platform.enums.EventTypeEnum;
import com.iot.platform.enums.LoginMethodEnum;
import com.iot.platform.enums.LoginResultEnum;
import com.iot.platform.mapper.LoginLogMapper;
import com.iot.platform.service.LoginLogService;
import com.iot.platform.vo.LoginLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 登录日志 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;

    private static final int EXPORT_MAX_ROWS = 10000;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== 查询 ====================

    @Override
    public PageResult<LoginLogVO> queryPage(LoginLogQueryDTO queryDTO) {
        LambdaQueryWrapper<LoginLog> wrapper = buildQueryWrapper(queryDTO);
        wrapper.orderByDesc(LoginLog::getLoginTime);

        Page<LoginLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<LoginLog> result = loginLogMapper.selectPage(page, wrapper);

        List<LoginLogVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    @Override
    public LoginLogVO getDetailById(Long id) {
        LoginLog loginLog = loginLogMapper.selectById(id);
        if (loginLog == null) {
            return null;
        }
        // 数据权限校验：非平台管理员不能查看其他租户的数据
        checkDataPermission(loginLog);
        return convertToVO(loginLog);
    }

    // ==================== 导出 ====================

    @Override
    public void exportCsv(LoginLogQueryDTO queryDTO, HttpServletResponse response) {
        // 限制导出条数
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(EXPORT_MAX_ROWS);

        LambdaQueryWrapper<LoginLog> wrapper = buildQueryWrapper(queryDTO);
        wrapper.orderByDesc(LoginLog::getLoginTime);
        wrapper.last("LIMIT " + EXPORT_MAX_ROWS);

        List<LoginLog> records = loginLogMapper.selectList(wrapper);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=login_log.csv");

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            // BOM for Excel UTF-8
            writer.print('\ufeff');
            // 表头
            writer.println("ID,用户ID,用户名,租户ID,登录时间,IP,登录方式,事件类型,结果,失败原因,设备类型,登录地点,会话ID,请求ID");
            // 数据行
            for (LoginLog r : records) {
                writer.printf("%d,%d,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        r.getId(),
                        r.getUserId(),
                        escapeCsv(r.getUsername()),
                        r.getTenantId(),
                        r.getLoginTime() != null ? r.getLoginTime().format(FMT) : "",
                        escapeCsv(r.getIp()),
                        descOfMethod(r.getLoginMethod()),
                        descOfEvent(r.getEventType()),
                        descOfResult(r.getResult()),
                        escapeCsv(r.getFailReason()),
                        escapeCsv(r.getDeviceType()),
                        escapeCsv(r.getLocation()),
                        escapeCsv(r.getSessionId()),
                        escapeCsv(r.getRequestId()));
            }
            writer.flush();
        } catch (IOException e) {
            log.error("导出登录日志 CSV 失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    // ==================== 异步写入 ====================

    @Async
    @Override
    public void asyncSave(LoginLog loginLog) {
        try {
            loginLogMapper.insert(loginLog);
            log.debug("异步写入登录日志成功, userId={}, username={}", loginLog.getUserId(), loginLog.getUsername());
        } catch (Exception e) {
            log.error("异步写入登录日志失败, loginLog={}", loginLog, e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建查询条件（含数据权限隔离）
     */
    private LambdaQueryWrapper<LoginLog> buildQueryWrapper(LoginLogQueryDTO dto) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();

        // ---- 业务筛选条件 ----
        wrapper.like(StringUtils.isNotBlank(dto.getUsername()), LoginLog::getUsername, dto.getUsername());
        wrapper.eq(StringUtils.isNotBlank(dto.getIp()), LoginLog::getIp, dto.getIp());
        wrapper.eq(dto.getResult() != null, LoginLog::getResult, dto.getResult());
        wrapper.eq(dto.getEventType() != null, LoginLog::getEventType, dto.getEventType());
        wrapper.eq(dto.getLoginMethod() != null, LoginLog::getLoginMethod, dto.getLoginMethod());
        wrapper.ge(dto.getStartTime() != null, LoginLog::getLoginTime, dto.getStartTime());
        wrapper.le(dto.getEndTime() != null, LoginLog::getLoginTime, dto.getEndTime());

        // ---- 数据权限隔离 ----
        applyDataPermission(wrapper);

        return wrapper;
    }

    /**
     * 根据当前用户角色自动追加数据权限过滤条件
     * <ul>
     *     <li>平台管理员：可查看所有数据</li>
     *     <li>租户管理员：只能查看本租户数据</li>
     *     <li>普通用户：只能查看自己的数据</li>
     * </ul>
     */
    private void applyDataPermission(LambdaQueryWrapper<LoginLog> wrapper) {
        String role = SecurityContextHolder.getRole();
        if (SecurityContextHolder.isPlatformAdmin()) {
            // 平台管理员 — 不做过滤
            return;
        }
        if (SecurityContextHolder.isTenantAdmin()) {
            // 租户管理员 — 按 tenant_id 过滤
            wrapper.eq(LoginLog::getTenantId, SecurityContextHolder.getTenantId());
        } else {
            // 普通用户 — 按 tenant_id + user_id 过滤
            wrapper.eq(LoginLog::getTenantId, SecurityContextHolder.getTenantId());
            wrapper.eq(LoginLog::getUserId, SecurityContextHolder.getUserId());
        }
    }

    /**
     * 详情查询的权限校验
     */
    private void checkDataPermission(LoginLog loginLog) {
        if (SecurityContextHolder.isPlatformAdmin()) {
            return;
        }
        Long currentTenantId = SecurityContextHolder.getTenantId();
        if (currentTenantId != null && !currentTenantId.equals(loginLog.getTenantId())) {
            throw new RuntimeException("无权访问该条记录");
        }
        if (!SecurityContextHolder.isTenantAdmin()) {
            Long currentUserId = SecurityContextHolder.getUserId();
            if (currentUserId != null && !currentUserId.equals(loginLog.getUserId())) {
                throw new RuntimeException("无权访问该条记录");
            }
        }
    }

    /**
     * Entity → VO 转换
     */
    private LoginLogVO convertToVO(LoginLog entity) {
        LoginLogVO vo = new LoginLogVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setLoginMethodDesc(descOfMethod(entity.getLoginMethod()));
        vo.setEventTypeDesc(descOfEvent(entity.getEventType()));
        vo.setResultDesc(descOfResult(entity.getResult()));
        return vo;
    }

    private String descOfMethod(Integer code) {
        return Optional.ofNullable(LoginMethodEnum.of(code)).map(LoginMethodEnum::getDesc).orElse("");
    }

    private String descOfEvent(Integer code) {
        return Optional.ofNullable(EventTypeEnum.of(code)).map(EventTypeEnum::getDesc).orElse("");
    }

    private String descOfResult(Integer code) {
        return Optional.ofNullable(LoginResultEnum.of(code)).map(LoginResultEnum::getDesc).orElse("");
    }

    /**
     * CSV 字段转义：包含逗号/引号/换行时用双引号包裹
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
