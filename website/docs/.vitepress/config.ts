import { defineConfig } from 'vitepress'
// import { groupIconMdPlugin, groupIconVitePlugin } from 'vitepress-plugin-group-icons'
import {
  GitChangelog,
  GitChangelogMarkdownSection,
} from '@nolebase/vitepress-plugin-git-changelog/vite'

import footnote from 'markdown-it-footnote'
import mark from 'markdown-it-mark'
import sub from 'markdown-it-sub'
import taskLists from 'markdown-it-task-lists'

export default defineConfig({
  title: 'MakoSU',
  description: 'Reliable kernel-root management for GKI Android devices.',
  base: '/MakoSU/',
  lang: 'zh-Hans',
  locales: {
    root: {
      label: '简体中文',
      lang: 'zh-Hans',
      themeConfig: {
        nav: [
          { text: '首页', link: '/' },
          { text: '文档', link: '/guide/' },
          { text: '功能', link: '/#features' },
          { text: '兼容性', link: '/#compatibility' },
        ],
        sidebar: {
          '/guide/': [
            {
              text: 'MakoSU 文档',
              items: [{ text: '文档首页', link: '/guide/' }],
            },
            {
              text: '安装与兼容',
              items: [
                { text: '安装与更新', link: '/guide/installation' },
                { text: 'KMI 兼容性', link: '/guide/compatibility' },
              ],
            },
            {
              text: '使用与恢复',
              items: [
                { text: 'SuSFS 使用', link: '/guide/susfs' },
                { text: '故障排查与救砖', link: '/guide/troubleshooting' },
              ],
            },
          ],
        },
        outline: { label: '本页目录', level: [2, 3] },
        docFooter: { prev: '上一篇', next: '下一篇' },
        lastUpdated: {
          text: '最后更新',
          formatOptions: { dateStyle: 'medium', timeStyle: 'short' },
        },
        sidebarMenuLabel: '文档导航',
        returnToTopLabel: '返回顶部',
        darkModeSwitchLabel: '外观',
        langMenuLabel: '切换语言',
        search: {
          provider: 'local',
          options: {
            translations: {
              button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' },
              modal: {
                noResultsText: '没有找到相关结果',
                resetButtonTitle: '清除查询',
                footer: { selectText: '选择', navigateText: '切换', closeText: '关闭' },
              },
            },
          },
        },
      },
    },
    en: {
      label: 'English',
      lang: 'en-US',
      title: 'MakoSU',
      description: 'Reliable kernel-root management for GKI Android devices.',
      themeConfig: {
        nav: [
          { text: 'Home', link: '/en/' },
          { text: 'Docs', link: '/en/guide/' },
          { text: 'Features', link: '/en/#features' },
          { text: 'Compatibility', link: '/en/#compatibility' },
        ],
        sidebar: {
          '/en/guide/': [
            {
              text: 'MakoSU Docs',
              items: [{ text: 'Documentation home', link: '/en/guide/' }],
            },
            {
              text: 'Install and compatibility',
              items: [
                { text: 'Installation and updates', link: '/en/guide/installation' },
                { text: 'KMI compatibility', link: '/en/guide/compatibility' },
              ],
            },
            {
              text: 'Use and recovery',
              items: [
                { text: 'Using SuSFS', link: '/en/guide/susfs' },
                { text: 'Troubleshooting and recovery', link: '/en/guide/troubleshooting' },
              ],
            },
          ],
        },
        outline: { label: 'On this page', level: [2, 3] },
        docFooter: { prev: 'Previous', next: 'Next' },
        lastUpdated: {
          text: 'Last updated',
          formatOptions: { dateStyle: 'medium', timeStyle: 'short' },
        },
        sidebarMenuLabel: 'Documentation menu',
        returnToTopLabel: 'Return to top',
        darkModeSwitchLabel: 'Appearance',
        langMenuLabel: 'Change language',
        search: { provider: 'local' },
      },
    },
  },

  lastUpdated: true,
  cleanUrls: true,
  metaChunk: true,

  cacheDir: './.vitepress/cache',
  ignoreDeadLinks: false,

  markdown: {
    math: true,
    config(md) {
      // md.use(groupIconMdPlugin)
      md.use(footnote)
      md.use(mark)
      md.use(sub)
      md.use(taskLists)
    },
    linkify: true,
    typographer: true,
    lineNumbers: true,
    image: {
      lazyLoading: true,
    },
    toc: {
      level: [1, 2, 3],
    },
    theme: {
      light: 'github-light',
      dark: 'github-dark',
    },
  },
  sitemap: {
    hostname: 'https://spring-bulid.github.io/MakoSU',
    transformItems(items) {
      return items.filter((item) => !item.url.includes('/en/'))
    },
  },

  head: [
    ['link', { rel: 'icon', href: '/favicon-96x96.png' }],
    ['link', { rel: 'apple-touch-icon', href: '/apple-touch-icon.png' }],
    ['link', { rel: 'manifest', href: '/site.webmanifest' }],
    ['meta', { name: 'theme-color', content: '#2f7259' }],
    ['meta', { name: 'og:type', content: 'website' }],
    ['meta', { name: 'og:locale', content: 'zh-Hans' }],
    ['meta', { name: 'og:site_name', content: 'MakoSU' }],
    ['meta', { name: 'twitter:card', content: 'summary_large_image' }],
    ['meta', { name: 'referrer', content: 'strict-origin-when-cross-origin' }],

    ['meta', {
      name: 'keywords',
      content: 'MakoSU, Android root, KernelSU, SuSFS, KMI, KPM, GKI, Android kernel, root management',
    }],
    ['meta', { name: 'author', content: 'MakoSU maintainers' }],

    ['script', { type: 'application/ld+json' }, JSON.stringify({
      '@context': 'https://schema.org',
      '@type': 'SoftwareApplication',
      name: 'MakoSU',
      description: 'Reliable kernel-root management for GKI Android devices',
      applicationCategory: 'SystemApplication',
      operatingSystem: 'Android',
      url: 'https://spring-bulid.github.io/MakoSU/',
      downloadUrl: 'https://github.com/Spring-bulid/MakoSU',
      supportingData: { '@type': 'DataCatalog', name: 'Compatibility Database' },
      offers: { '@type': 'Offer', price: '0', priceCurrency: 'USD' },
      author: { '@type': 'Organization', name: 'MakoSU maintainers', url: 'https://github.com/Spring-bulid' },
    })],

    ['meta', { name: 'apple-mobile-web-app-capable', content: 'yes' }],
    ['meta', { name: 'apple-mobile-web-app-status-bar-style', content: 'black-translucent' }],
    ['meta', { name: 'apple-mobile-web-app-title', content: 'MakoSU' }],
  ],

  themeConfig: {
    logo: { src: '/makosu-manager.png', width: 28, height: 28 },
    socialLinks: [{ icon: 'github', link: 'https://github.com/Spring-bulid/MakoSU' }],
    editLink: {
      pattern: 'https://github.com/Spring-bulid/MakoSU/edit/main/website/docs/:path',
      text: '在 GitHub 上编辑此页',
    },
  },

  vite: {
    plugins: [
      groupIconVitePlugin({
        customIcon: {
          bash: '<svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256"><g fill="none"><rect width="256" height="256" fill="#242938" rx="60"/><path fill="#242938" fill-rule="evenodd" d="m203.819 68.835l-63.14-37.48a23.79 23.79 0 0 0-24.361 0l-63.14 37.48C45.642 73.31 41 81.575 41 90.522v74.961c0 8.945 4.643 17.215 12.18 21.689l63.14 37.473a23.8 23.8 0 0 0 12.179 3.354a23.8 23.8 0 0 0 12.178-3.354l63.14-37.473c7.536-4.474 12.182-12.744 12.182-21.689v-74.96c0-8.948-4.646-17.214-12.18-21.688" clip-rule="evenodd"/><path fill="#fff" fill-rule="evenodd" d="m118.527 220.808l-63.14-37.474c-6.176-3.666-10.013-10.506-10.013-17.852V90.523c0-7.346 3.837-14.186 10.01-17.85l63.143-37.48a19.55 19.55 0 0 1 9.972-2.747c3.495 0 6.943.95 9.973 2.747l63.14 37.48c5.204 3.089 8.714 8.438 9.701 14.437c-2.094-4.469-6.817-5.684-12.32-2.47l-59.734 36.897c-7.448 4.354-12.94 9.24-12.945 18.221v73.604c-.004 5.378 2.168 8.861 5.504 9.871c-1.096.19-2.201.322-3.319.322a19.55 19.55 0 0 1-9.972-2.747m85.292-151.974l-63.14-37.478A23.8 23.8 0 0 0 128.499 28a23.8 23.8 0 0 0-12.181 3.356l-63.14 37.478C45.642 73.308 41 81.576 41 90.524v74.958c0 8.945 4.643 17.215 12.18 21.689l63.14 37.475A23.84 23.84 0 0 0 128.499 228a23.83 23.83 0 0 0 12.178-3.354l63.142-37.475c7.536-4.474 12.18-12.744 12.18-21.689V90.523c0-8.947-4.644-17.215-12.18-21.689" clip-rule="evenodd"/><path fill="#47b353" fill-rule="evenodd" d="m187.267 172.729l-15.722 9.41c-.417.243-.723.516-.726 1.017v4.114c0 .503.338.712.754.467l15.966-9.703c.416-.243.48-.708.483-1.209v-3.629c0-.5-.338-.71-.755-.467" clip-rule="evenodd"/><path fill="#242938" fill-rule="evenodd" d="M153.788 138.098c.509-.258.928.059.935.725l.053 5.439c2.277-.906 4.255-1.148 6.047-.734c.389.104.561.633.402 1.261l-1.197 4.82c-.093.364-.298.732-.545.961a1.3 1.3 0 0 1-.315.234a.7.7 0 0 1-.472.077c-.818-.185-2.763-.61-5.823.94c-3.21 1.625-4.333 4.414-4.311 6.484c.027 2.472 1.295 3.221 5.673 3.296c5.834.097 8.355 2.646 8.416 8.522c.06 5.77-3.02 11.966-7.732 15.763l.104 5.384c.006.648-.415 1.391-.924 1.649l-3.189 1.837c-.511.258-.93-.06-.937-.708l-.055-5.296c-2.731 1.135-5.499 1.409-7.267.699c-.333-.13-.476-.622-.344-1.182l1.156-4.868c.092-.384.295-.768.571-1.012q.147-.142.299-.219c.183-.092.362-.112.514-.055c1.905.642 4.342.342 6.685-.844c2.977-1.506 4.968-4.542 4.937-7.558c-.029-2.737-1.51-3.874-5.113-3.901c-4.586.013-8.861-.891-8.932-7.642c-.057-5.558 2.833-11.342 7.408-14.999l-.057-5.435c-.007-.668.401-1.403.926-1.667z" clip-rule="evenodd"/></g></svg>',
        },
      }),
      GitChangelog({ repoURL: () => 'https://github.com/Spring-bulid/MakoSU' }),
      GitChangelogMarkdownSection({
        exclude: (id) => id.endsWith('index.md'),
        sections: { disableContributors: true },
      }),
    ],
    build: {
      minify: 'terser',
      chunkSizeWarningLimit: 800,
      assetsInlineLimit: 8192,
      target: 'esnext',
      cssCodeSplit: true,
      sourcemap: false,
    },
    server: {
      fs: { allow: ['..'] },
    },
  },
})

