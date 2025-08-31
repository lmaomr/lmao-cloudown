// 统一管理API基础URL配置
const config = {

    // 从环境变量获取API基础URL，若不存在则使用默认值
    //   apiBaseUrl: `${window.location.protocol}//${window.location.hostname}:${window.location.port || (window.location.protocol === 'https:' ? 443 : 80)}/api`,
    apiBaseUrl: `${window.location.protocol}//${window.location.hostname}:8080`,
    // 如需添加其他配置项，可在此处扩展
};

export default config;
