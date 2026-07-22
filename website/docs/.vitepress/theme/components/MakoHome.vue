<script setup lang="ts">
import { computed } from 'vue'
import { useData, withBase } from 'vitepress'

const { site } = useData()
const isZh = computed(() => !site.value.lang.toLowerCase().startsWith('en'))
const docsUrl = computed(() => withBase(isZh.value ? '/guide/' : '/en/guide/'))

const githubUrl = 'https://github.com/Spring-bulid/MakoSU'

const copy = computed(() =>
  isZh.value
    ? {
        eyebrow: 'MAKOSU MANAGER / GKI 2.0',
        title: '把复杂的内核 Root 维护，收进一个可靠的管理器里。',
        lead: 'MakoSU 统一管理权限、KMI 匹配、SuSFS、KPM、模块与内核刷写，并让签名身份和发布模块保持一致。',
        start: '了解功能',
        docs: '查看文档',
        source: '查看源码',
        facts: [
          ['Android 8.0+', '管理器最低版本'],
          ['5.10 - 6.12', '正式 KMI 范围'],
          ['7 组 KMI', '随发布包提供'],
          ['3 种 ABI', 'Rust 用户空间组件'],
        ],
        featureKicker: 'MANAGER CAPABILITIES',
        featureTitle: '日常操作更直接，失败路径更可控',
        featureLead:
          '功能围绕真实维护场景组织：识别设备、选择正确模块、管理隐藏配置，并为刷写失败保留恢复空间。',
        features: [
          ['01', '智能 KMI 匹配', '读取 Android KMI 标记并匹配对应 LKM，避免只看内核主版本强行加载。'],
          ['02', 'SuSFS 集中管理', '统一管理隐藏路径、映射、stat、vname、日志与自动启动配置。'],
          ['03', '可恢复的模块更新', '新模块先暂存和校验，启用失败时尝试恢复旧配置与旧模块状态。'],
          ['04', '签名身份契约', '管理器包名、APK Release 证书与内核预期哈希作为同一发布契约维护。'],
          ['05', 'KPM 与刷写工具', '提供 KPM、模块管理、启动镜像修补和内核刷写入库。'],
          ['06', 'Material + Miuix', '两套管理界面、主题切换和备用图标，兼顾信息密度与操作效率。'],
        ],
        kmiKicker: 'RELEASE KMI SET',
        kmiTitle: '七组正式 KMI，不靠模糊匹配碰运气',
        kmiLead:
          '当前发布面向 GKI 2.0。厂商 ABI、符号、配置和 KMI 标记仍需一致；5.4/GKI 1.0 暂不列入正式发布。',
        kmiAction: '查看发布身份契约',
        susfsKicker: 'SUSFS USERSPACE',
        susfsTitle: '配置可靠性优先于功能堆叠',
        susfsLead:
          'MakoSU 对 SuSFS 用户空间链路做了事务化处理，减少卡顿、并发覆盖和启动阶段的不可恢复状态。',
        susfsItems: [
          ['原子保存', '临时文件、fsync 与原子替换降低异常中断后的配置损坏风险。'],
          ['严格解析', '拒绝截断数据、重复字段、超长内容和尾随垃圾数据。'],
          ['单次读取', '管理器通过一次 Root 调用读取完整配置，减少连续拉起命令造成的界面卡顿。'],
          ['失败回滚', '自动启动模块和备份恢复在失败时尽量回到上一个可用状态。'],
        ],
        contractKicker: 'RELEASE CONTRACT',
        contractTitle: '管理器与内核必须认同同一个身份',
        contractLead:
          '更换包名或证书不是单独的界面修改。任何身份字段发生变化，都需要重新构建 KMI 并验证 APK v2 证书。',
        contract: [
          ['应用包名', 'com.makosu.manager'],
          ['证书 DER 大小', '0x0549'],
          ['Release KMI', '7'],
          ['用户空间 ABI', 'arm64 / armv7 / x86_64'],
        ],
        safetyTitle: '刷写前，先准备好恢复路径',
        safetyLead:
          '错误的内核、LKM、签名身份或目标分区都可能导致设备无法启动。请备份原始镜像，并确认 Fastboot 或 Recovery 可用。',
        safetyAction: '查看源码',
        copyright:
          '与 Senren Banka 相关的角色及视觉素材归属于 YUZUSOFT 及其各自权利持有人。MakoSU 为非官方维护项目。',
      }
    : {
        eyebrow: 'MAKOSU MANAGER / GKI 2.0',
        title: 'Reliable kernel-root management packed into one clean manager.',
        lead: 'MakoSU keeps permissions, KMI matching, SuSFS, KPM, modules, and kernel flashing consistent under a single release contract.',
        start: 'Explore features',
        docs: 'Read the docs',
        source: 'View source',
        facts: [
          ['Android 8.0+', 'Min manager version'],
          ['5.10 - 6.12', 'Official KMI support'],
          ['7 KMI groups', 'Bundled per release'],
          ['3 ABIs', 'Rust userspace components'],
        ],
        featureKicker: 'MANAGER CAPABILITIES',
        featureTitle: 'Surgical daily operations with clear failure stories',
        featureLead:
          'Features are organised around real maintenance workflows: identify the device, pick the right module, manage hiding configs, and preserve recovery paths when flashing fails.',
        features: [
          ['01', 'Smart KMI matching', 'Reads Android KMI markers and loads the corresponding LKM instead of forcing based on kernel version alone.'],
          ['02', 'Centralized SuSFS', 'Unified management of hidden paths, mappings, stat, vname, logs, and auto-start configs.'],
          ['03', 'Recoverable module updates', 'New modules are staged and validated first; on failure, the manager restores the previous config and module state.'],
          ['04', 'Signature identity contract', 'The package name, APK Release certificate, and expected kernel hash are maintained as a single release contract.'],
          ['05', 'KPM and flashing tools', 'Provides KPM, module management, boot image patching, and kernel flashing.'],
          ['06', 'Material + Miuix', 'Two management UIs, theme switching, and fallback icons — balancing information density with operational efficiency.'],
        ],
        kmiKicker: 'RELEASE KMI SET',
        kmiTitle: 'Seven formal KMI variants — no fuzzy matching',
        kmiLead:
          'Current releases target GKI 2.0. Vendor ABI, symbols, config, and KMI markers must still match; 5.4 / GKI 1.0 is not included in the formal release set.',
        kmiAction: 'View release contract',
        susfsKicker: 'SUSFS USERSPACE',
        susfsTitle: 'Configuration reliability over feature stacking',
        susfsLead:
          'MakoSU applies transactional handling to the SuSFS userspace chain, reducing jank, concurrent overwrites, and unrecoverable states during boot.',
        susfsItems: [
          ['Atomic save', 'Temporary files, fsync, and atomic replacement reduce the risk of config corruption after abnormal interruption.'],
          ['Strict parsing', 'Rejects truncated data, duplicate fields, oversized content, and trailing garbage.'],
          ['Single read', 'The manager reads the entire config in one root call, reducing UI lag caused by repeated command invocations.'],
          ['Failure rollback', 'Auto-start modules and backup recovery attempt to return to the last known good state on failure.'],
        ],
        contractKicker: 'RELEASE CONTRACT',
        contractTitle: 'The manager and kernel must agree on one identity',
        contractLead:
          'Changing the package name or certificate is not a cosmetic UI change. Any identity field change requires rebuilding the KMI and re-verifying the APK v2 certificate.',
        contract: [
          ['Application ID', 'com.makosu.manager'],
          ['Certificate DER size', '0x0549'],
          ['Release KMI variants', '7'],
          ['Userspace ABI', 'arm64 / armv7 / x86_64'],
        ],
        safetyTitle: 'Prepare a recovery path before flashing',
        safetyLead:
          'A mismatched kernel, LKM, signing identity, or target partition can make a device unbootable. Keep the original image and a working Fastboot or Recovery path.',
        safetyAction: 'View source',
        copyright:
          'Characters and visual material related to Senren Banka belong to YUZUSOFT and their respective rightsholders. MakoSU is an unofficial maintenance project.',
      }
)

const kmiRows = [
  ['Android 12', '5.10', 'android12-5.10'],
  ['Android 13', '5.10', 'android13-5.10'],
  ['Android 13', '5.15', 'android13-5.15'],
  ['Android 14', '5.15', 'android14-5.15'],
  ['Android 14', '6.1', 'android14-6.1'],
  ['Android 15', '6.6', 'android15-6.6'],
  ['Android 16', '6.12', 'android16-6.12'],
]
</script>

<template>
  <div class="mako-home">
    <!-- ═══ HERO ═══ -->
    <section class="mako-hero">
      <div class="mako-hero-bg"></div>
      <img class="mako-hero-art" :src="withBase('/makosu-manager.png')" alt="" aria-hidden="true" />
      <div class="mako-shell mako-hero-content">
        <p class="mako-kicker">{{ copy.eyebrow }}</p>
        <h1 class="mako-hero-name">MakoSU</h1>
        <p class="mako-hero-tagline">
          <span class="mako-hero-line"></span>
          {{ copy.title }}
        </p>
        <p class="mako-hero-lead">{{ copy.lead }}</p>
        <div class="mako-actions">
          <a class="mako-btn mako-btn-primary" href="#features">{{ copy.start }} →</a>
          <a class="mako-btn mako-btn-alt" :href="docsUrl">{{ copy.docs }}</a>
          <a class="mako-btn mako-btn-alt" :href="githubUrl">{{ copy.source }}</a>
        </div>
        <dl class="mako-facts">
          <div v-for="fact in copy.facts" :key="fact[0]">
            <dt>{{ fact[0] }}</dt>
            <dd>{{ fact[1] }}</dd>
          </div>
        </dl>
      </div>
    </section>

    <!-- ═══ FEATURES ═══ -->
    <section id="features" class="mako-box">
      <div class="mako-shell">
        <header class="mako-section-heading">
          <p class="mako-kicker">{{ copy.featureKicker }}</p>
          <h2>{{ copy.featureTitle }}</h2>
          <p>{{ copy.featureLead }}</p>
        </header>
        <div class="mako-feature-grid">
          <article v-for="feature in copy.features" :key="feature[0]" class="mako-feature">
            <span class="mako-feature-index">{{ feature[0] }}</span>
            <h3>{{ feature[1] }}</h3>
            <p>{{ feature[2] }}</p>
          </article>
        </div>
      </div>
    </section>

    <!-- ═══ KMI COMPATIBILITY ═══ -->
    <section id="compatibility" class="mako-box mako-box-alt">
      <div class="mako-shell mako-split">
        <div class="mako-split-copy">
          <p class="mako-kicker">{{ copy.kmiKicker }}</p>
          <h2>{{ copy.kmiTitle }}</h2>
          <p>{{ copy.kmiLead }}</p>
          <a class="mako-text-link" href="#release-contract">{{ copy.kmiAction }} →</a>
        </div>
        <div class="mako-kmi-list" role="table" aria-label="MakoSU KMI support matrix">
          <div v-for="row in kmiRows" :key="row[2]" class="mako-kmi-row" role="row">
            <span role="cell">{{ row[0] }}</span>
            <strong role="cell">{{ row[1] }}</strong>
            <code role="cell">{{ row[2] }}</code>
          </div>
        </div>
      </div>
    </section>

    <!-- ═══ SUSFS ═══ -->
    <section id="susfs" class="mako-box mako-box-dark">
      <div class="mako-shell mako-split">
        <div class="mako-split-copy">
          <p class="mako-kicker mako-kicker-light">{{ copy.susfsKicker }}</p>
          <h2>{{ copy.susfsTitle }}</h2>
          <p>{{ copy.susfsLead }}</p>
        </div>
        <div class="mako-susfs-list">
          <div v-for="item in copy.susfsItems" :key="item[0]">
            <h3>{{ item[0] }}</h3>
            <p>{{ item[1] }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ═══ CONTRACT ═══ -->
    <section id="release-contract" class="mako-box mako-box-alt">
      <div class="mako-shell">
        <header class="mako-section-heading mako-section-heading-wide">
          <p class="mako-kicker">{{ copy.contractKicker }}</p>
          <h2>{{ copy.contractTitle }}</h2>
          <p>{{ copy.contractLead }}</p>
        </header>
        <dl class="mako-contract-grid">
          <div v-for="item in copy.contract" :key="item[0]">
            <dt>{{ item[0] }}</dt>
            <dd>{{ item[1] }}</dd>
          </div>
        </dl>
      </div>
    </section>

    <!-- ═══ SAFETY ═══ -->
    <section class="mako-box mako-box-dark">
      <div class="mako-shell mako-safety-inner">
        <div>
          <p class="mako-kicker mako-kicker-light">RECOVERY FIRST</p>
          <h2>{{ copy.safetyTitle }}</h2>
          <p>{{ copy.safetyLead }}</p>
        </div>
        <a class="mako-btn mako-btn-primary" :href="githubUrl">{{ copy.safetyAction }} →</a>
      </div>
    </section>

    <!-- ═══ FOOTER ═══ -->
    <footer class="mako-home-footer">
      <div class="mako-shell">
        <img :src="withBase('/makosu-brand.png')" alt="MakoSU" />
        <p>{{ copy.copyright }}</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* ═══════════════════════════════════════════════════
   MakoSU Home — FolkPatch Plume Style
   ALL original sections preserved
   ═══════════════════════════════════════════════════ */

.mako-home {
  width: 100%;
}

/* ── Layout ── */
.mako-shell {
  width: min(1152px, calc(100% - 48px));
  margin: 0 auto;
}

/* ── Content Box (FolkPatch home-box) ── */
.mako-box {
  padding: 64px 0;
}

.mako-box-alt {
  background: var(--vp-c-bg-soft);
}

.mako-box-dark {
  background: #1a1d23;
  color: #fff;
}

[data-theme='dark'] .mako-box-dark {
  background: #111316;
}

.mako-box-dark h2 {
  color: #fff;
}

.mako-box-dark p {
  color: #a0a8b4;
}

/* ── Kicker ── */
.mako-kicker {
  margin: 0 0 12px;
  color: var(--vp-c-brand-1);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.mako-kicker-light {
  color: var(--vp-c-brand-1) !important;
}

/* ── Hero ── */
.mako-hero {
  position: relative;
  display: flex;
  min-height: calc(100svh - 64px);
  overflow: hidden;
  background: var(--vp-c-bg);
  border-bottom: 1px solid var(--vp-c-divider);
}

.mako-hero-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 80% 60% at 50% 40%, var(--vp-c-brand-soft) 0%, transparent 70%);
  pointer-events: none;
}

.mako-hero-art {
  position: absolute;
  top: 50%;
  right: max(3vw, 24px);
  width: min(48vw, 600px);
  height: min(64vh, 600px);
  object-fit: contain;
  transform: translateY(-48%);
  opacity: 0.85;
}

.mako-hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  padding: 64px 0 48px;
}

/* Hero Name — Gradient text (FolkPatch Plume) */
.mako-hero-name {
  margin: 0;
  font-size: clamp(48px, 8vw, 100px);
  font-weight: 900;
  line-height: 1;
  letter-spacing: -0.5px;
  background: linear-gradient(315deg, #a78bfa 15%, #8b5cf6 65%, #7c3aed 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  width: fit-content;
}

[data-theme='dark'] .mako-hero-name {
  background: linear-gradient(315deg, #c4b5fd 15%, #a78bfa 65%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Hero Tagline — with decorative line (FolkPatch Plume) */
.mako-hero-tagline {
  display: flex;
  align-items: center;
  max-width: 640px;
  margin: 1rem 0 0;
  font-size: clamp(20px, 3vw, 28px);
  font-weight: 600;
  line-height: 1.25;
  color: var(--vp-c-text-1);
}

.mako-hero-line {
  display: inline-block;
  width: 80px;
  height: 0;
  border-top: 1px solid var(--vp-c-text-2);
  margin-right: 1rem;
  flex-shrink: 0;
}

.mako-hero-lead {
  max-width: 600px;
  margin: 1.25rem 0 0;
  font-size: clamp(14px, 1.5vw, 16px);
  font-weight: 500;
  line-height: 1.75;
  color: var(--vp-c-text-2);
}

/* ── Actions ── */
.mako-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 24px;
}

.mako-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none !important;
  transition: all 0.15s ease;
}

.mako-btn:hover {
  transform: translateY(-1px);
}

.mako-btn-primary {
  background: var(--vp-c-brand-1);
  color: #fff !important;
}

.mako-btn-primary:hover {
  background: var(--vp-c-brand-2);
  box-shadow: 0 4px 14px rgba(139, 92, 246, 0.35);
}

.mako-btn-alt {
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1) !important;
  border: 1px solid var(--vp-c-divider);
}

.mako-btn-alt:hover {
  border-color: var(--vp-c-brand-1);
}

/* ── Facts ── */
.mako-facts {
  display: grid;
  width: min(640px, 100%);
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  margin: 36px 0 0;
  padding: 0;
  border-top: 1px solid var(--vp-c-divider);
}

.mako-facts > div {
  min-width: 0;
  padding: 14px 16px 0 0;
}

.mako-facts dt {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--vp-c-text-1);
}

.mako-facts dd {
  margin: 4px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--vp-c-text-3);
}

/* ── Section Headings ── */
.mako-section-heading {
  max-width: 720px;
  margin-bottom: 40px;
}

.mako-section-heading-wide {
  max-width: 880px;
}

.mako-box h2,
.mako-section-heading h2 {
  margin: 0;
  font-size: 38px;
  font-weight: 750;
  line-height: 1.2;
  letter-spacing: -0.01em;
  color: var(--vp-c-text-1);
}

.mako-section-heading > p:last-child,
.mako-split-copy > p:last-of-type {
  margin: 16px 0 0;
  font-size: 15px;
  line-height: 1.75;
  color: var(--vp-c-text-2);
}

/* ── Feature Grid ── */
.mako-feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.mako-feature {
  min-height: 220px;
  padding: 28px;
  background: var(--vp-c-bg);
  border: 1px solid var(--vp-c-divider);
  border-radius: 10px;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.mako-feature:hover {
  border-color: var(--vp-c-brand-1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

[data-theme='dark'] .mako-feature:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.mako-feature-index {
  font-size: 13px;
  font-weight: 800;
  color: var(--vp-c-brand-1);
}

.mako-feature h3,
.mako-susfs-list h3 {
  margin: 20px 0 0;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--vp-c-text-1);
}

.mako-feature p,
.mako-susfs-list p {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--vp-c-text-2);
}

/* ── KMI Matrix ── */
.mako-split {
  display: grid;
  grid-template-columns: minmax(0, 0.8fr) minmax(500px, 1.2fr);
  gap: 64px;
  align-items: start;
}

.mako-split-copy {
  position: sticky;
  top: 88px;
}

.mako-text-link {
  display: inline-flex;
  margin-top: 24px;
  color: var(--vp-c-brand-1) !important;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none !important;
}

.mako-text-link:hover {
  color: var(--vp-c-brand-2) !important;
}

.mako-kmi-list {
  border-top: 2px solid var(--vp-c-text-1);
}

.mako-kmi-row {
  display: grid;
  grid-template-columns: 1fr 90px minmax(170px, 1fr);
  gap: 16px;
  align-items: center;
  min-height: 58px;
  border-bottom: 1px solid var(--vp-c-divider);
  font-size: 14px;
  color: var(--vp-c-text-1);
}

.mako-kmi-row strong {
  color: var(--vp-c-brand-1);
}

.mako-kmi-row code {
  padding: 0;
  background: transparent;
  font-size: 13px;
  color: var(--vp-c-text-2);
}

/* ── SuSFS ── */
.mako-susfs-list {
  border-top: 1px solid #404850;
}

.mako-susfs-list > div {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 24px;
  padding: 22px 0;
  border-bottom: 1px solid #363d44;
}

.mako-susfs-list h3 {
  margin: 0;
  color: #fff;
  font-size: 16px;
}

.mako-susfs-list p {
  color: #b0b8c0;
}

/* ── Contract ── */
.mako-contract-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin: 0;
  padding: 0;
  border-top: 2px solid var(--vp-c-text-1);
}

.mako-contract-grid > div {
  min-width: 0;
  padding: 22px 22px 0 0;
}

.mako-contract-grid dt {
  font-size: 13px;
  color: var(--vp-c-text-3);
}

.mako-contract-grid dd {
  margin: 6px 0 0;
  font-family: var(--vp-font-family-mono);
  font-size: 14px;
  font-weight: 700;
  color: var(--vp-c-text-1);
}

/* ── Safety ── */
.mako-safety-inner {
  display: flex;
  gap: 40px;
  align-items: center;
  justify-content: space-between;
}

.mako-safety-inner > div {
  max-width: 740px;
}

.mako-safety-inner .mako-btn {
  flex: 0 0 auto;
}

/* ── Footer ── */
.mako-home-footer {
  padding: 48px 0 56px;
  background: var(--vp-c-bg);
  border-top: 1px solid var(--vp-c-divider);
}

.mako-home-footer .mako-shell {
  display: flex;
  gap: 28px;
  align-items: center;
}

.mako-home-footer img {
  width: 160px;
  height: auto;
  opacity: 0.7;
}

.mako-home-footer p {
  max-width: 640px;
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
  color: var(--vp-c-text-3);
}

/* ── Responsive ── */
@media (max-width: 960px) {
  .mako-hero-art {
    right: -8%;
    width: 56vw;
    opacity: 0.28;
  }

  .mako-feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .mako-split {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .mako-split-copy {
    position: static;
  }

  .mako-contract-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .mako-shell {
    width: min(100% - 32px, 1152px);
  }

  .mako-hero {
    min-height: calc(100svh - 80px);
  }

  .mako-hero-art {
    top: auto;
    right: -14%;
    bottom: -4%;
    width: 100vw;
    height: 56vh;
    opacity: 0.14;
    transform: none;
  }

  .mako-hero-content {
    padding: 48px 0 32px;
  }

  .mako-hero-line {
    width: 40px;
    margin-right: 0.75rem;
  }

  .mako-actions,
  .mako-btn {
    width: 100%;
  }

  .mako-box {
    padding: 48px 0;
  }

  .mako-section-heading h2 {
    font-size: 29px;
  }

  .mako-feature-grid {
    grid-template-columns: 1fr;
  }

  .mako-feature {
    min-height: 0;
  }

  .mako-kmi-row {
    grid-template-columns: 1fr 56px;
    gap: 10px;
    padding: 12px 0;
  }

  .mako-kmi-row code {
    grid-column: 1 / -1;
  }

  .mako-susfs-list > div {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .mako-contract-grid {
    grid-template-columns: 1fr;
  }

  .mako-contract-grid > div {
    padding: 16px 0;
    border-bottom: 1px solid var(--vp-c-divider);
  }

  .mako-safety-inner,
  .mako-home-footer .mako-shell {
    flex-direction: column;
    align-items: flex-start;
  }

  .mako-home-footer img {
    width: min(200px, 65vw);
  }
}
</style>