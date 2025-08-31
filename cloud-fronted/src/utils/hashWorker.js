// 接收主线程消息
// hashWorker.js
// 接收主线程消息
self.onmessage = function (e) {
    const { chunk, chunkIndex, totalChunks } = e.data;

    try {
        const fileReader = new FileReader();

        fileReader.onload = function (event) {
            try {
                const buffer = event.target.result;

                // 发送ArrayBuffer数据回主线程
                self.postMessage({
                    success: true,
                    chunkIndex,
                    buffer,
                    size: chunk.size,
                    totalChunks
                });
            } catch (error) {
                self.postMessage({
                    success: false,
                    chunkIndex,
                    error: error.message
                });
            }
        };

        fileReader.onerror = function () {
            self.postMessage({
                success: false,
                chunkIndex,
                error: 'Failed to read file chunk'
            });
        };

        // 开始读取文件块为ArrayBuffer
        fileReader.readAsArrayBuffer(chunk);
    } catch (error) {
        self.postMessage({
            success: false,
            chunkIndex,
            error: error.message
        });
    }
};
