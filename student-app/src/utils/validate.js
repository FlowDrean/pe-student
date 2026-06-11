/* ============================================
   表单验证规则
   ============================================ */

// 手机号验证
export const isPhone = (value) => /^1[3-9]\d{9}$/.test(value)

// 邮箱验证
export const isEmail = (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)

// 用户名验证（2-20位，字母数字下划线汉字）
export const isUsername = (value) => /^[\w一-龥]{2,20}$/.test(value)

// 密码强度（6-20位）
export const isPassword = (value) => value.length >= 6 && value.length <= 20

// 学号验证
export const isStudentId = (value) => /^\d{6,20}$/.test(value)

// 验证规则集
export const rules = {
  phone: [
    { required: true, message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
  ],
  email: [
    { required: true, message: '请输入邮箱' },
    { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '邮箱格式不正确' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, max: 20, message: '密码长度为6-20位' },
  ],
  username: [
    { required: true, message: '请输入用户名' },
    { pattern: /^[\w一-龥]{2,20}$/, message: '用户名为2-20位字母、数字、下划线或汉字' },
  ],
  required: (name) => [
    { required: true, message: `请输入${name}` },
  ],
}
