package com.iot.platform.service;

import com.iot.platform.common.PageResult;
import com.iot.platform.dto.LoginLogQueryDTO;
import com.iot.platform.entity.LoginLog;
import com.iot.platform.vo.LoginLogVO;

import javax.servlet.http.HttpServletResponse;

/**
 * 登录日志 Service 接口
 */
public interface LoginLogService {

    /**
     * 分页查询登录日志（含数据权限隔离）
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<LoginLogVO> queryPage(LoginLogQueryDTO queryDTO);

    /**
     * 根据 ID 查询登录日志详情
     *
     * @param id 日志 ID
     * @return 日志详情 VO
     */
    LoginLogVO getDetailById(Long id);

    /**
     * 导出登录日志为 CSV（上限 10000 条）
     *
     * @param queryDTO 查询参数
     * @param response HTTP 响应对象
     */
    void exportCsv(LoginLogQueryDTO queryDTO, HttpServletResponse response);

    /**
     * 异步写入登录日志
     *
     * @param loginLog 日志实体
     */
    void asyncSave(LoginLog loginLog);
}
