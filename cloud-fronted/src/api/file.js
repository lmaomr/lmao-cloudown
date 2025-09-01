import request from '@/api/request'

export const getFileList = (path, category, sort) => {
  return request({
    url: '/api/file/list',
    method: 'get',
    params: {
      path: path || '/',
      category: category || 'my-files', // 默认显示所有类型
      sort: sort || 'time-desc', // 默认按名称升序排序
    },
  })
}



export const checkUploadTask = (quickHash) => {
  return request({
    url: '/api/file/check-upload',
    method: 'get',
    params: { quickHash }
  })
}

export const uploadFile = (filename, chunk, index, totalChunks, options = {}) => {
  const formData = new FormData()
  formData.append('filename', filename)
  formData.append('file', chunk.chunk)
  formData.append('index', index)
  formData.append('totalChunks', totalChunks)
  return request({
    url: '/api/file/upload',
    method: 'post',
    data: formData,
    onUploadProgress: options.onUploadProgress, // 添加进度回调
  })
}

export const mergeFileChunks = (filename, size, chunksCount, path) => {
  const formData = new FormData()
  formData.append('filename', filename)
  formData.append('size', size)
  formData.append('chunksCount', chunksCount)
  formData.append('path', path || 'my-files')
  return request({
    url: '/api/file/merge',
    method: 'post',
    data: formData,
  })
}

//上传头像
const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('avatar', file)
  return request({
    url: '/api/file/uploadAvatar',
    method: 'post',
    data: formData,
  })
}

export const createFolder = (folderName, path) => {
  return request({
    url: '/api/file/createFolder',
    method: 'post',
    params: {
      folderName,
      path: path || 'my-files/',
    },
  })
}

const createTextFile = (fileName, path) => {
  return request({
    url: '/api/file/createFile',
    method: 'post',
    params: {
      fileName: fileName,
      path: path || 'my-files/',
    },
  })
}

export default {
  getFileList,
  checkUploadTask,
  uploadFile,
  mergeFileChunks,
  uploadAvatar,
  createFolder,
  createTextFile
}
