import request from '@/utils/request'

/**
 * 查询登录日志列表
 * @param {Object} params - 查询参数
 * @param {string}  [params.username]  - 用户名
 * @param {string}  [params.ip]        - IP 地址
 * @param {string}  [params.startTime] - 开始时间 (yyyy-MM-dd HH:mm:ss)
 * @param {string}  [params.endTime]   - 结束时间
 * @param {number}  [params.result]    - 登录结果 1=成功 0=失败
 * @param {number}  [params.pageNum=1] - 页码
 * @param {number}  [params.pageSize=20] - 每页条数
 */
export function getLoginLogList(params) {
  return request({
    url: '/api/v1/login-logs',
    method: 'get',
    params
  })
}

/**
 * 获取登录日志详情
 * @param {number|string} id - 日志 ID
 */
export function getLoginLogDetail(id) {
  return request({
    url: `/api/v1/login-logs/${id}`,
    method: 'get'
  })
}

/**
 * 导出登录日志 CSV
 * @param {Object} params - 与列表查询相同的筛选参数（不含分页）
 */
export function exportLoginLogCsv(params) {
  return request({
    url: '/api/v1/login-logs/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
