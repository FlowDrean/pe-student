import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
// Vant 样式按需导入（仅项目实际使用的组件，替代全量 vant/lib/index.css）
import 'vant/es/toast/style'
import 'vant/es/dialog/style'
import 'vant/es/image-preview/style'
import 'vant/es/pull-refresh/style'
import 'vant/es/list/style'
import 'vant/es/swipe/style'
import 'vant/es/swipe-item/style'
import 'vant/es/swipe-cell/style'
import './assets/styles/global.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
