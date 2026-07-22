<script setup lang="ts">
import { computed } from 'vue'
import { useData, withBase } from 'vitepress'

const { site } = useData()
const isZh = computed(() => !site.value.lang.toLowerCase().startsWith('en'))

const githubUrl = 'https://github.com/Spring-bulid/MakoSU'
const docsUrl = computed(() => withBase(isZh.value ? '/guide/' : '/en/guide/'))

const t = computed(() => isZh.value ? {
  tagline: 'MakoSU — 面向 GKI 设备的可靠内核 Root 管理器',
  description: '统一管理权限、KMI 匹配、SuSFS、KPM、模块与内核刷写，并保持签名身份和发布模块一致。',
  docs: '阅读完整文档',
  intro: '介绍',
  introContent: 'MakoSU 是一款基于 KernelSU 的内核 Root 管理工具，专注于提供直观可靠的设备管理体验。无论是日常权限管理、模块刷写还是内核配置，都能在一个界面内完成。',
  features: '核心功能',
  featuresList: [
    { title: '智能 KMI 匹配', desc: '读取 Android KMI 标记并匹配对应 LKM，避免只看内核主版本强行加载。' },
    { title: 'SuSFS 集中管理', desc: '统一管理隐藏路径、映射、stat、vname、日志与自动启动配置。' },
    { title: '可恢复的模块更新', desc: '新模块先暂存和校验，启用失败时尝试恢复旧配置与旧模块状态。' },
    { title: '签名身份契约', desc: '管理器包名、APK 证书与内核预期哈希作为同一发布契约维护。' },
    { title: 'KPM 与刷写工具', desc: '提供 KPM、模块管理、启动镜像修补和内核刷写入库。' },
    { title: '双界面主题', desc: 'Material 与 Miuix 两套管理界面，兼顾信息密度与操作效率。' },
  ],
  requirements: '前置要求',
  requirementsText: '基于 ARM64 架构，Linux 内核版本 5.10 至 6.12 的 Android 设备（Android 8.0+）。',
  uiDesign: '管理器的界面与设计',
  uiDesignList: [
    '全新的 Material 与 Miuix 双界面切换',
    '个性化壁纸与主题支持',
    '国际化多语言支持',
    '动画性能与交互流畅度优化',
    '界面视觉细节与动态效果提升',
    '手动关闭自动更新检查',
  ],
  modules: '模块相关',
  modulesList: [
    'APM: 类 Magisk 模块系统，支持批量刷入与全量备份',
    'KPM: 内核模块系统，支持 inline-hook 与 syscall-table-hook，支持自动加载',
    '模块在线下载、安装与管理',
  ],
  tech: '技术特性',
  techText: '基于 KernelSU 实现，无需重新编译内核即可加载内核模块。',
  download: '下载安装',
  downloadText: '从 GitHub Releases 下载最新版本安装包，安装到你的 Android 设备即可开始使用。',
  thanks: '开源致谢',
  thanksText: '本项目基于 KernelSU、KernelPatch、Magisk、APatch 等开源项目。',
  license: '许可证',
  licenseText: 'MakoSU 遵循 GPL-3.0 许可证开源。',
  community: '社区交流',
  communityText: 'Telegram 频道: @MakoSU',
} : {
  tagline: 'MakoSU — Reliable kernel-root management for GKI Android devices',
  description: 'Unified permission management, KMI matching, SuSFS, KPM, modules, and kernel flashing with consistent signature identity.',
  docs: 'Read the full documentation',
  intro: 'Introduction',
  introContent: 'MakoSU is a kernel root management tool based on KernelSU, focused on providing an intuitive and reliable device management experience.',
  features: 'Core Features',
  featuresList: [
    { title: 'Smart KMI matching', desc: 'Reads the Android KMI marker and loads the corresponding LKM.' },
    { title: 'Centralized SuSFS', desc: 'Manages hidden paths, mappings, stat, vname, logs, and auto-start configs.' },
    { title: 'Recoverable module updates', desc: 'New modules are staged and validated first; restore previous state on failure.' },
    { title: 'Signature identity contract', desc: 'Package name, APK certificate, and kernel hash as a single release contract.' },
    { title: 'KPM and flashing tools', desc: 'KPM, module management, boot image patching, and kernel flashing.' },
    { title: 'Dual UI themes', desc: 'Material and Miuix management interfaces for different workflows.' },
  ],
  requirements: 'Requirements',
  requirementsText: 'ARM64-based Android device with Linux kernel 5.10–6.12 (Android 8.0+).',
  uiDesign: 'UI & Design',
  uiDesignList: [
    'Brand new Material & Miuix dual UI',
    'Personalized wallpaper and theme support',
    'Internationalization / multi-language',
    'Optimized animation performance',
    'Enhanced visual details and dynamic effects',
    'Manual update check toggle',
  ],
  modules: 'Modules',
  modulesList: [
    'APM: Magisk-like module system with batch flash and full backup',
    'KPM: Kernel module system with inline-hook and syscall-table-hook, auto-load support',
    'Online module download, install, and management',
  ],
  tech: 'Technical',
  techText: 'Based on KernelSU — load kernel modules without recompiling the kernel.',
  download: 'Download & Install',
  downloadText: 'Download the latest release from GitHub Releases and install on your Android device.',
  thanks: 'Acknowledgements',
  thanksText: 'Built on KernelSU, KernelPatch, Magisk, APatch and other open source projects.',
  license: 'License',
  licenseText: 'MakoSU is open-sourced under GPL-3.0 license.',
  community: 'Community',
  communityText: 'Telegram: @MakoSU',
})
</script>

<template>
  <div class="mako-home">
    <!-- Hero — centered, FolkPatch-style -->
    <section class="mako-hero">
      <div class="mako-shell">
        <img class="mako-logo" :src="withBase('/makosu-manager.png')" alt="MakoSU" />

        <div class="mako-badges">
          <a :href="githubUrl">
            <img src="https://img.shields.io/github/v/release/Spring-bulid/MakoSU?label=Release&logo=github" alt="Release" />
          </a>
          <a href="https://github.com/Spring-bulid/MakoSU/blob/main/LICENSE">
            <img src="https://img.shields.io/github/license/Spring-bulid/MakoSU?logo=gnu" alt="License" />
          </a>
        </div>

        <p class="mako-lang-links">
          <a href="/MakoSU/"><strong>中文</strong></a>
          &nbsp;/&nbsp;
          <a href="/MakoSU/en/">English</a>
        </p>

        <p class="mako-tagline">{{ t.tagline }}</p>
        <p class="mako-description">{{ t.description }}</p>

        <a class="mako-docs-link" :href="docsUrl">
          {{ t.docs }} <span class="mako-arrow">→</span>
        </a>

        <!-- Screenshots grid -->
        <div class="mako-screenshots">
          <div class="mako-screenshot"><span>Screenshot 1</span></div>
          <div class="mako-screenshot"><span>Screenshot 2</span></div>
          <div class="mako-screenshot"><span>Screenshot 3</span></div>
          <div class="mako-screenshot"><span>Screenshot 4</span></div>
          <div class="mako-screenshot"><span>Screenshot 5</span></div>
          <div class="mako-screenshot"><span>Screenshot 6</span></div>
        </div>
      </div>
    </section>

    <div class="mako-divider"></div>

    <!-- Content sections — matches FolkPatch README flow -->
    <section class="mako-content">
      <div class="mako-shell mako-content-inner">
        <h2>{{ t.intro }}</h2>

        <h3>{{ t.features }}</h3>
        <ul>
          <li v-for="f in t.featuresList" :key="f.title">
            <strong>{{ f.title }}</strong> — {{ f.desc }}
          </li>
        </ul>

        <h3>{{ t.requirements }}</h3>
        <ul>
          <li><strong>必须：</strong> {{ t.requirementsText }}</li>
        </ul>

        <h3>{{ t.uiDesign }}</h3>
        <ul>
          <li v-for="(item, i) in t.uiDesignList" :key="i">[x] {{ item }}</li>
        </ul>

        <h3>{{ t.modules }}</h3>
        <ul>
          <li v-for="(item, i) in t.modulesList" :key="i">[x] {{ item }}</li>
        </ul>

        <h3>{{ t.tech }}</h3>
        <p>[x] {{ t.techText }}</p>

        <h2>{{ t.download }}</h2>
        <ol>
          <li><strong>下载安装：</strong> 从 <a :href="githubUrl + '/releases/latest'">发布页面</a> 下载最新版安装包</li>
          <li><strong>安装应用：</strong> 安装最新版安装包到你的 Android 设备</li>
          <li><strong>开始使用：</strong> 阅读 <a :href="docsUrl">{{ isZh ? '在线文档' : 'online docs' }}</a></li>
        </ol>

        <h2>{{ t.thanks }}</h2>
        <p>{{ t.thanksText }}</p>

        <h2>{{ t.license }}</h2>
        <p>{{ t.licenseText }}</p>

        <h2>{{ t.community }}</h2>
        <p>{{ t.communityText }}</p>
      </div>
    </section>

    <footer class="mako-footer">
      <div class="mako-shell">
        <p>Created by Senren Bright and MakoSU maintainers</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* ── Reset ── */
.mako-home {
  width: 100%;
  margin: 0;
  padding: 0;
}

.mako-shell {
  width: min(960px, calc(100% - 48px));
  margin: 0 auto;
  text-align: center;
}

/* ── Hero ── */
.mako-hero {
  padding: 80px 0 64px;
  text-align: center;
}

.mako-logo {
  width: min(500px, 80vw);
  height: auto;
  margin: 0 auto 28px;
  display: block;
}

.mako-badges {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.mako-badges img {
  height: 20px;
  display: block;
}

.mako-lang-links {
  margin: 0 0 24px;
  font-size: 14px;
  color: var(--vp-c-text-2);
}

.mako-lang-links a {
  color: var(--vp-c-text-2);
  text-decoration: none;
}

.mako-lang-links a:hover,
.mako-lang-links strong {
  color: var(--vp-c-brand-1);
}

.mako-tagline {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--vp-c-text-1);
}

.mako-description {
  max-width: 600px;
  margin: 0 auto 28px;
  font-size: 15px;
  line-height: 1.7;
  color: var(--vp-c-text-2);
}

.mako-docs-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  background: var(--vp-c-brand-1);
  color: #fff !important;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none !important;
  transition: all 0.15s ease;
}

.mako-docs-link:hover {
  background: var(--vp-c-brand-2);
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.35);
}

.mako-arrow {
  font-size: 18px;
}

/* ── Screenshots ── */
.mako-screenshots {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 48px;
}

.mako-screenshot {
  aspect-ratio: 9/19;
  background: var(--vp-c-bg-soft);
  border: 1px solid var(--vp-c-divider);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--vp-c-text-3);
  font-size: 13px;
  overflow: hidden;
}

/* ── Divider ── */
.mako-divider {
  width: min(960px, calc(100% - 48px));
  height: 1px;
  margin: 0 auto;
  background: var(--vp-c-divider);
}

/* ── Content ── */
.mako-content {
  padding: 64px 0;
}

.mako-content-inner {
  text-align: left;
  max-width: 780px;
}

.mako-content-inner :deep(h2) {
  margin: 48px 0 16px;
  padding-bottom: 8px;
  font-size: 24px;
  font-weight: 700;
  border-bottom: 1px solid var(--vp-c-divider);
  color: var(--vp-c-text-1);
}

.mako-content-inner :deep(h3) {
  margin: 28px 0 12px;
  font-size: 18px;
  font-weight: 650;
  color: var(--vp-c-text-1);
}

.mako-content-inner :deep(p) {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.75;
  color: var(--vp-c-text-2);
}

.mako-content-inner :deep(ul),
.mako-content-inner :deep(ol) {
  margin: 8px 0 16px;
  padding-left: 24px;
}

.mako-content-inner :deep(li) {
  margin: 6px 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--vp-c-text-2);
}

.mako-content-inner :deep(strong) {
  color: var(--vp-c-text-1);
}

.mako-content-inner :deep(a) {
  color: var(--vp-c-brand-1);
}

/* ── Footer ── */
.mako-footer {
  padding: 40px 0;
  background: var(--vp-c-bg-soft);
  border-top: 1px solid var(--vp-c-divider);
}

.mako-footer p {
  margin: 0;
  font-size: 12px;
  color: var(--vp-c-text-3);
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .mako-hero {
    padding: 48px 0 40px;
  }

  .mako-tagline {
    font-size: 18px;
  }

  .mako-screenshots {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .mako-content {
    padding: 48px 0;
  }
}

@media (max-width: 480px) {
  .mako-shell {
    width: min(100% - 32px, 960px);
  }

  .mako-screenshots {
    grid-template-columns: 1fr;
  }
}
</style>