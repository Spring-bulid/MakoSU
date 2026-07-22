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
          'Features are organised around real maintenance workflows: identify the device, select the right module, manage hiding configuration, and keep recovery space for failed flashes.',
        features: [
          ['01', 'Smart KMI matching', 'Reads the Android KMI marker and selects the matching LKM instead of guessing from the kernel major version.'],
          ['02', 'Unified SuSFS management', 'Configure paths, maps, stat, vname, logging, and auto-start from one surface.'],
          ['03', 'Recoverable module updates', 'New modules are staged and verified; if activation fails the manager rolls back to the last-known-good module and configuration.'],
          ['04', 'Signature identity contract', 'Manager package name, APK Release certificate, and kernel-side expected hash are maintained as one release contract.'],
          ['05', 'KPM & flash tooling', 'KPM, module management, boot-image patching, and kernel flashing from a single entry point.'],
          ['06', 'Material + Miuix', 'Two UI skins, theme switching, and fallback icons balance information density with operational speed.'],
        ],
        kmiKicker: 'RELEASE KMI SET',
        kmiTitle: 'Seven official KMI groups, not fuzzy matching',
        kmiLead:
          'This release targets GKI 2.0. OEM ABI, symbols, configuration, and KMI marker must still align; 5.4/GKI 1.0 is not part of the official release set. ',
        kmiAction: 'See the release contract',
        susfsKicker: 'SUSFS USERSPACE',
        susfsTitle: 'Configuration safety before feature count',
        susfsLead:
          'MakoSU wraps SuSFS userspace operations in transactional helpers to reduce jank, concurrent overwrites, and unrecoverable states during boot.',
        susfsItems: [
          ['Atomic writes', 'Temp files, fsync, and atomic rename reduce corruption risk after unexpected interruption.'],
          ['Strict parsing', 'Rejects truncated data, duplicate keys, oversized values, and trailing garbage.'],
          ['Single-shot read', 'The manager reads the full configuration with a single root call, avoiding UI lag from repeated shell invocations.'],
          ['Failure rollback', 'Auto-start modules and backup restore try to get back to the last usable state when something goes wrong.'],
        ],
        contractKicker: 'RELEASE CONTRACT',
        contractTitle: 'Manager and kernel must agree on identity',
        contractLead:
          'Changing the package name or certificate is not a cosmetic edit. Any identity field change requires a proper KMI rebuild and APK v2 certificate verification.',
        contract: [
          ['Package name', 'com.makosu.manager'],
          ['Certificate DER size', '0x0549'],
          ['Release KMIs', '7'],
          ['Userspace ABIs', 'arm64 / armv7 / x86_64'],
        ],
        safetyTitle: 'Prepare recovery before flashing',
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
