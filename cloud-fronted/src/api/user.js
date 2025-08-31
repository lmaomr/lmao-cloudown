import request from '@/api/request';

const login = (data) => {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

const register = (data) => {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

const getUserInfo = () => {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
}

const updateNickname = (nickname) => {
  return request({
    url: '/api/user/update/nickname',
    method: 'put',
    data: { nickname }
  })
}

const updatePassword = (oldPassword, newPassword) => {
  return request({
    url: '/api/user/update/password',
    method: 'put',
    data: { oldPassword, newPassword }
  })
}

export default {
  login,
  register,
  getUserInfo,
  updateNickname,
  updatePassword
}

