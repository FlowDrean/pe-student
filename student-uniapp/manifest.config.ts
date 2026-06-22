import { defineManifestConfig } from '@uni-helper/vite-plugin-uni-pages'

export default defineManifestConfig({
  name: '校园互助',
  appid: '',
  description: 'PE-Student 校园互助平台',
  versionName: '1.0.0',
  versionCode: '100',
  transformPx: false,
  compileDebug: true,
  locale: 'zh-Hans',
  permission: {},
  requiredPrivateInfos: ['chooseImage', 'chooseLocation'],
  mpWeixin: {
    appid: '',
    setting: {
      urlCheck: false,
      es6: true,
      minified: true,
      postcss: true,
    },
    usingComponents: true,
    permission: {
      'scope.userLocation': { desc: '用于展示附近的交易信息' },
    },
  },
})
