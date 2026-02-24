<template>
  <el-container style="height: 100vh;">
    <el-header style="background: #304156; display: flex; align-items: center; padding: 0 20px;">
      <h1 style="color: #fff; font-size: 18px; margin: 0;">🌐 IoT 平台管理系统</h1>
      <el-menu
        mode="horizontal"
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        style="margin-left: 40px; border-bottom: none;"
      >
        <el-menu-item index="/system/login-log">📋 登录日志</el-menu-item>
      </el-menu>
    </el-header>
    <el-main style="padding: 20px; background: #f0f2f5;">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
</style>
