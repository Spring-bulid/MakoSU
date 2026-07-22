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
        title: '把复杂的内核 Root 维护，收进一个可靠的管理器里',
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
        featureLead: '功能围绕真实维护场景组织：识别设备、选择正确模块、管理隐藏配置，并为刷写失败保留恢复空间。',
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
        kmiLead: '当前发布面向 GKI 2.0。厂商 ABI、符号、配置和 KMI 标记仍需一致；5.4/GKI 1.0 暂不列入正式发布。',
        kmiAction: '查看发布身份契约',
        susfsKicker: 'SUSFS USERSPACE',
        susfsTitle: '配置可靠性优先于功能堆叠',
        susfsLead: 'MakoSU 对 SuSFS 用户空间链路做了事务化处理，减少卡顿、并发覆盖和启动阶段的不可恢复状态。',
        susfsItems: [
          ['原子保存', '临时文件、fsync 与原子替换降低异常中断后的配置损坏风险。'],
          ['严格解析', '拒绝截断数据、重复字段、超长内容和尾随垃圾数据。'],
          ['单次读取', '管理器通过一次 Root 调用读取完整配置，减少连续拉起命令造成的界面卡顿。'],
          ['失败回滚', '自动启动模块和备份恢复在失败时尽量回到上一个可用状态。'],
        ],
        contractKicker: 'RELEASE CONTRACT',
        contractTitle: '管理器与内核必须认同同一个身份',
        contractLead: '更换包名或证书不是单独的界面修改。任何身份字段发生变化，都需要重新构建 KMI 并验证 APK v2 证书。',
        contract: [
          ['应用包名', 'com.makosu.manager'],
          ['证书 DER 大小', '0x0549'],
          ['Release KMI', '7'],
          ['用户空间 ABI', 'arm64 / armv7 / x86_64'],
        ],
        safetyTitle: '刷写前，先准备好恢复路径',
        safetyLead: '错误的内核、LKM、签名身份或目标分区都可能导致设备无法启动。请备份原始镜像，并确认 Fastboot 或 Recovery 可用。',
        safetyAction: '查看源码',
        copyright: '由 Senren Bright 与 MakoSU 维护者创建 · 按发布契约分发 · 所有代码公开可审查',
      }
    : {
        eyebrow: 'MAKOSU MANAGER / GKI 2.0',
        title: 'Put complex kernel root maintenance into one reliable manager.',
        lead: 'MakoSU unifies permission management, KMI matching, SuSFS, KPM, modules, and kernel flashing while keeping signature identity and release artifacts consistent.',
        start: 'See features',
        docs: 'Read docs',
        source: 'View source',
        facts: [
          ['Android 8.0+', 'Minimum manager version'],
          ['5.10 – 6.12', 'Formal KMI range'],
          ['7 KMI variants', 'Included per release'],
          ['3 ABI targets', 'Rust userspace components'],
        ],
        featureKicker: 'MANAGER CAPABILITIES',
        featureTitle: 'Direct daily operations, controlled failure paths',
        featureLead: 'The feature set is organized around real maintenance workflows: identify the device, pick the right module, manage hiding configs, and preserve recovery paths when flashing fails.',
        features: [
          ['01', 'Smart KMI matching', 'Reads the Android KMI marker and loads the corresponding LKM instead of forcing a load based only on the kernel major version.'],
          ['02', 'Centralized SuSFS', 'Manages hidden paths, mappings, stat, vname, logs, and auto-start configs in one place.'],
          ['03', 'Recoverable module updates', 'New modules are staged and validated first; on failure, the manager tries to restore the previous config and module state.'],
          ['04', 'Signature identity contract', 'The package name, APK Release certificate, and expected kernel hash are maintained as a single release contract.'],
          ['05', 'KPM and flashing tools', 'Provides KPM, module management, boot image patching, and kernel flashing.'],
          ['06', 'Material + Miuix', 'Two management UIs, theme switching, and fallback icons — balancing information density with operational efficiency.'],
        ],
        kmiKicker: 'RELEASE KMI SET',
        kmiTitle: 'Seven formal KMI variants — no fuzzy matching',
        kmiLead: 'Current releases target GKI 2.0. Vendor ABI, symbols, config, and KMI markers must still match; 5.4 / GKI 1.0 is not included in the formal release set.',
        kmiAction: 'View release contract',
        susfsKicker: 'SUSFS USERSPACE',
        susfsTitle: 'Configuration reliability over feature stacking',
        susfsLead: 'MakoSU applies transactional handling to the SuSFS userspace chain, reducing jank, concurrent overwrites, and unrecoverable states during boot.',
        susfsItems: [
          ['Atomic save', 'Temporary files, fsync, and atomic replacement reduce the risk of config corruption after abnormal interruption.'],
          ['Strict parsing', 'Rejects truncated data, duplicate fields, oversized content, and trailing garbage.'],
          ['Single read', 'The manager reads the entire config in one root call, reducing UI lag caused by repeated command invocations.'],
          ['Failure rollback', 'Auto-start modules and backup recovery attempt to return to the last known good state on failure.'],
        ],
        contractKicker: 'RELEASE CONTRACT',
        contractTitle: 'The manager and kernel must agree on one identity',
        contractLead: 'Changing the package name or certificate is not a cosmetic UI change. Any identity field change requires rebuilding the KMI and re-verifying the APK v2 certificate.',
        contract: [
          ['Application ID', 'com.makosu.manager'],
          ['Certificate DER size', '0x0549'],
          ['Release KMI variants', '7'],
          ['Userspace ABI', 'arm64 / armv7 / x86_64'],
        ],
        safetyTitle: 'Prepare a recovery path before flashing',
        safetyLead: 'Wrong kernel, LKM, signature identity, or target partition can leave a device unable to boot. Back up the original image and confirm Fastboot or Recovery is available.',
        safetyAction: 'View source',
        copyright: 'Created by Senren Bright and MakoSU maintainers · Distributed per the release contract · All source is open for review',
      }
)

const kmiRows = [
  ['Android 13', '5.15', 'android13-5.15'],
  ['Android 14', '6.1', 'android14-6.1'],
  ['Android 15', '6.6', 'android15-6.6'],
  ['Android 16', '6.12', 'android16-6.12'],
]
</script>

<template>
  <div class="mako-home">
    <section class="mako-hero">
      <img class="mako-hero-art" :src="withBase('/makosu-manager.png')" alt="" aria-hidden="true" />
      <div class="mako-shell mako-hero-content">
        <p class="mako-kicker">{{ copy.eyebrow }}</p>
        <h1>MakoSU</h1>
        <p class="mako-hero-title">{{ copy.title }}</p>
        <p class="mako-hero-lead">{{ copy.lead }}</p>
        <div class="mako-actions">
          <a class="mako-button mako-button-primary" href="#features">{{ copy.start }} →</a>
          <a class="mako-button mako-button-secondary" :href="docsUrl">{{ copy.docs }}</a>
          <a class="mako-button mako-button-secondary" :href="githubUrl">{{ copy.source }}</a>
        </div>
        <dl class="mako-facts">
          <div v-for="fact in copy.facts" :key="fact[0]">
            <dt>{{ fact[0] }}</dt>
            <dd>{{ fact[1] }}</dd>
          </div>
        </dl>
      </div>
    </section>

    <section id="features" class="mako-section mako-feature-section">
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

    <section id="compatibility" class="mako-section mako-kmi-section">
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

    <section id="susfs" class="mako-section mako-susfs-section">
      <div class="mako-shell mako-split">
        <div class="mako-split-copy">
          <p class="mako-kicker">{{ copy.susfsKicker }}</p>
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

    <section id="release-contract" class="mako-section mako-contract-section">
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

    <section class="mako-section mako-safety-section">
      <div class="mako-shell mako-safety-inner">
        <div>
          <p class="mako-kicker">RECOVERY FIRST</p>
          <h2>{{ copy.safetyTitle }}</h2>
          <p>{{ copy.safetyLead }}</p>
        </div>
        <a class="mako-button mako-button-light" :href="githubUrl">{{ copy.safetyAction }} →</a>
      </div>
    </section>

    <footer class="mako-home-footer">
      <div class="mako-shell">
        <img :src="withBase('/makosu-brand.png')" alt="MakoSU" />
        <p>{{ copy.copyright }}</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.mako-home {
  width: 100%;
}

.mako-kicker {
  color: var(--vp-c-brand-1);
}

.mako-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.mako-button-primary {
  background: var(--vp-c-brand-1) !important;
  color: #fff !important;
}

.mako-button-primary:hover {
  background: var(--vp-c-brand-2) !important;
}

.mako-button-secondary {
  background: var(--vp-c-bg-soft) !important;
  color: var(--vp-c-text-1) !important;
  border: 1px solid var(--vp-c-divider) !important;
}

.mako-button-light {
  background: var(--vp-c-brand-1) !important;
  color: #fff !important;
}

.mako-text-link {
  color: var(--vp-c-brand-1) !important;
}

.mako-text-link:hover {
  color: var(--vp-c-brand-2) !important;
}
</style>