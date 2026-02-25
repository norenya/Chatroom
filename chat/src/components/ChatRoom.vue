<template>
  <div class="chat-container">
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <span>聊天室</span>
        </div>
      </template>
      <div class="message-list" ref="messageList">
        <div v-for="(message, index) in messages" :key="index" class="message-item">
          <el-avatar :size="32" class="user-avatar">{{ message.user.charAt(0) }}</el-avatar>
          <div class="message-content">
            <div class="message-header">
              <span class="user-name">{{ message.user }}</span>
              <span class="message-time">{{ message.time }}</span>
            </div>
            <div class="message-text">{{ message.text }}</div>
          </div>
        </div>
      </div>
      <div class="chat-input-area">
        <el-input
          v-model="inputMessage"
          placeholder="请输入消息"
          @keyup.enter="sendMessage"
          class="message-input"
        ></el-input>
        <el-button type="primary" @click="sendMessage" class="send-button">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ChatRoom',
  data() {
    return {
      messages: [],
      inputMessage: '',
      ws: null,
      userName: '用户' + Math.floor(Math.random() * 10000)
    }
  },
  mounted() {
    this.initWebSocket()
  },
  beforeUnmount() {
    if (this.ws) {
      this.ws.close()
    }
  },
  methods: {
    initWebSocket() {
      const wsUrl = 'ws://localhost:8081/ws/chat'
      this.ws = new WebSocket(wsUrl)
      
      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
      }
      
      this.ws.onmessage = (event) => {
        const message = JSON.parse(event.data)
        this.messages.push(message)
        this.scrollToBottom()
      }
      
      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket连接关闭')
      }
    },
    sendMessage() {
      if (!this.inputMessage.trim()) return
      
      const message = {
        user: this.userName,
        text: this.inputMessage,
        time: new Date().toLocaleTimeString()
      }
      
      if (this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify(message))
        this.inputMessage = ''
      } else {
        console.error('WebSocket连接未就绪')
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const messageList = this.$refs.messageList
        messageList.scrollTop = messageList.scrollHeight
      })
    }
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.chat-card {
  width: 600px;
  height: 80vh;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  margin-bottom: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.user-avatar {
  margin-right: 10px;
}

.message-content {
  flex: 1;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.user-name {
  font-weight: bold;
  font-size: 14px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  font-size: 14px;
  line-height: 1.4;
}

.chat-input-area {
  display: flex;
  gap: 10px;
}

.message-input {
  flex: 1;
}

.send-button {
  min-width: 80px;
}
</style>
