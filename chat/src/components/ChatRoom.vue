<template>
  <div class="chat-app" @click="closeSidebarOnMobile">
    <!-- Mobile sidebar toggle button -->
    <el-button 
      class="mobile-sidebar-toggle"
      :icon="Menu"
      @click.stop="toggleSidebar"
    />
    
    <el-container class="chat-container">
      <!-- 侧边栏 -->
      <el-aside width="260px" class="sidebar" :class="{ open: isSidebarOpen }">
        <div class="sidebar-content">
          <!-- 顶部 Logo 区域 -->
          <div class="sidebar-header">
            <div class="brand">
              <el-avatar :size="40" class="brand-avatar">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
              <span class="brand-text">ChatRoom</span>
            </div>
          
            <el-tooltip content="退出登录" placement="bottom">
              <el-button 
                type="danger" 
                :icon="CloseBold"
                circle 
                size="small"
                @click="handleLogout"
                class="logout-btn"
              />
            </el-tooltip>
          </div>

          <!-- 频道区域 -->
          <div class="section">
            <div class="section-header">
              <el-text type="info" size="small" class="section-title">频道列表</el-text>
              <el-tooltip content="创建频道">
                <el-button 
                  type="primary" 
                  :icon="Plus"
                  link 
                  size="small"
                  @click="showCreateChannelDialog"
                />
              </el-tooltip>
            </div>
            
            <el-menu
              :default-active="currentChannel"
              class="channel-menu"
              @select="switchChannel"
            >
              <el-menu-item>
                <el-icon><ChatLineRound /></el-icon>
                <template #title>
                  <span>综合</span>
                </template>
              </el-menu-item>
              
              <el-menu-item 
                v-for="channel in channels" 
                :key="channel.id" 
                :index="channel.name"
              >
                <el-icon><ChatDotRound /></el-icon>
                <template #title>
                  <div class="channel-item-content">
                    <span>{{ channel.name }}</span>
                    <div class="channel-actions" v-if="channel.id !== 0">
                      <el-button 
                        link 
                        type="primary" 
                        size="small"
                        @click.stop="showEditChannelDialog(channel)"
                      >
                        <el-icon><Edit /></el-icon>
                      </el-button>
                      <el-button 
                        link 
                        type="danger" 
                        size="small"
                        @click.stop="deleteChannel(channel)"
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </template>
              </el-menu-item>
            </el-menu>
          </div>

          <!-- 在线用户区域 -->
          <div class="section">
            <div class="section-header">
              <el-text type="info" size="small" class="section-title">在线用户</el-text>
              <el-badge :value="onlineUsers.length" type="primary" class="user-count" />
            </div>
            
            <el-scrollbar class="user-list" max-height="300px">
              <div 
                v-for="onlineUser in onlineUsers" 
                :key="onlineUser.ID"
                class="user-item"
                :class="{ 'is-me': user && onlineUser.ID === user.ID }"
                @click="openPrivateChat(onlineUser)"
              >
                <el-badge is-dot :type="onlineUser.ID === user?.ID ? 'success' : 'primary'" class="user-status-badge">
                  <el-avatar :size="32" :src="onlineUser.avatar">
                    {{ onlineUser.Username.charAt(0).toUpperCase() }}
                  </el-avatar>
                </el-badge>
                <div class="user-info-text">
                  <el-text truncated class="user-name">{{ onlineUser.Username }}</el-text>
                  <el-text type="info" size="small" class="user-status">
                    {{ onlineUser.ID === user?.ID ? '我' : '在线' }}
                  </el-text>
                </div>
                <el-icon v-if="onlineUser.ID !== user?.ID" class="chat-icon"><ChatLineSquare /></el-icon>
              </div>
            </el-scrollbar>
          </div>
        </div>

        <!-- 底部用户信息 -->
        <div class="sidebar-footer" v-if="user">
          <el-card shadow="hover" class="user-card" @click="showUserProfile">
            <div class="user-profile">
              <el-badge is-dot type="success" class="avatar-badge">
                <el-avatar :size="40" :src="user.avatar">
                  {{ user.Username.charAt(0).toUpperCase() }}
                </el-avatar>
              </el-badge>
              <div class="user-meta">
                <el-text class="user-name" size="large">{{ user.Username }}</el-text>
                <el-text type="success" size="small">
                  <el-icon><CircleCheck /></el-icon> 在线
                </el-text>
              </div>
              <el-icon class="settings-icon"><Setting /></el-icon>
            </div>
          </el-card>
        </div>
      </el-aside>

      <!-- 主聊天区域 -->
      <el-main class="chat-main">
        <!-- 头部 -->
        <el-header class="chat-header" height="60px">
          <div class="header-left">
            <el-icon class="channel-icon" :size="24" color="#409EFF">
              <component :is="privateChatUser ? ChatLineSquare : ChatLineRound" />
            </el-icon>
            <div class="header-info">
              <el-text class="channel-name" size="large" tag="b">
                {{ privateChatUser ? privateChatUser.Username : (currentChannel === 'general' ? '综合频道' : currentChannel) }}
              </el-text>
              <el-text type="info" size="small" class="channel-desc">
                {{ privateChatUser ? '私聊中' : (currentChannel === 'general' ? '所有人都可以在此聊天' : (currentChannelDescription || '频道聊天')) }}
              </el-text>
            </div>
          </div>
          
          <div class="header-actions">
            <el-tooltip content="搜索消息">
              <el-button :icon="Search" circle />
            </el-tooltip>
            <el-tooltip content="成员列表">
              <el-button :icon="User" circle />
            </el-tooltip>
            <el-divider direction="vertical" />
            <el-dropdown placement="bottom-end">
              <el-button :icon="More" circle />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="toggleTheme">
                    <el-icon><SwitchButton /></el-icon>
                    {{ currentTheme === 'dark' ? '切换到浅色模式' : '切换到深色模式' }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    登出
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 消息区域 -->
        <div class="message-container" ref="messageContainer">
          <el-empty 
            v-if="messages.length === 0" 
            description="暂无消息！"
            :image-size="120"
          >
            <template #image>
              <el-icon :size="60" color="#dcdfe6"><ChatLineRound /></el-icon>
            </template>
          </el-empty>

          <el-scrollbar v-else ref="messageScrollbar" class="message-list" always>
            <div class="messages-wrapper">
              <div 
                v-for="(message, index) in messages" 
                :key="index" 
                class="message-item"
                :class="{ 
                  'own-message': user && message.senderId === user.ID,
                  'has-file': message.fileUrl
                }"
              >
                <!-- 时间分割线 -->
                <div 
                  v-if="index === 0 || isDifferentDay(messages[index-1], message)" 
                  class="time-divider"
                >
                  <el-divider>
                    <el-text type="info" size="small">{{ formatTime(message.time) }}</el-text>
                  </el-divider>
                </div>

                <div class="message-content-wrapper">
                  <el-avatar 
                    :size="40" 
                    :src="message.avatar"
                    class="message-avatar"
                  >
                    {{ getSenderName(message).charAt(0).toUpperCase() }}
                  </el-avatar>
                  
                  <div class="message-body">
                    <div class="message-header">
                      <el-text class="sender-name" tag="b">{{ getSenderName(message) }}</el-text>
                      <el-text type="info" size="small" class="message-time">
                        {{ formatTime(message.time) }}
                      </el-text>
                    </div>
                    
                    <!-- 文本消息 -->
                    <el-card 
                      v-if="message.text" 
                      shadow="never"
                      :class="['message-bubble', { 'own-bubble': user && message.senderId === user.ID }]"
                      :body-style="{ padding: '10px 14px' }"
                    >
                      <el-text class="message-text">{{ message.text }}</el-text>
                    </el-card>

                    <!-- 文件消息 -->
                    <el-card 
                      v-if="message.fileUrl" 
                      shadow="hover"
                      class="file-card"
                      :body-style="{ padding: '12px' }"
                    >
                      <div class="file-content">
                        <div class="file-icon">
                          <el-icon :size="32" color="#409EFF"><Document /></el-icon>
                        </div>
                        <div class="file-info">
                          <el-text class="file-name" truncated>{{ message.fileName }}</el-text>
                          <el-text type="info" size="small">点击下载文件</el-text>
                        </div>
                        <el-button 
                          type="primary" 
                          :icon="Download" 
                          circle 
                          size="small"
                          tag="a"
                          :href="message.fileUrl"
                          target="_blank"
                        />
                      </div>
                    </el-card>
                  </div>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <el-card shadow="never" class="input-card">
            <div class="input-toolbar">
              <el-tooltip content="上传文件">
                <el-upload
                  class="upload-btn"
                  action="http://localhost:8081/api/files/upload"
                  :on-success="handleFileUpload"
                  :show-file-list="false"
                  accept="*"
                  :headers="{ 'Content-Type': 'multipart/form-data' }"
                >
                  <el-button :icon="Upload" link />
                </el-upload>
              </el-tooltip>
              
              <el-divider direction="vertical" />
              
              <el-tooltip content="表情">
                <el-button :icon="Picture" link />
              </el-tooltip>
              
              <el-tooltip content="@某人">
                <el-button :icon="PriceTag" link />
              </el-tooltip>
            </div>

            <div class="input-wrapper">
              <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="2"
                placeholder="输入消息... 按 Enter 发送，Shift + Enter 换行"
                resize="none"
                @keydown.enter.prevent="handleEnter"
                class="message-input"
              />
              <el-button 
                type="primary" 
                :icon="Promotion" 
                class="send-btn"
                :disabled="!inputMessage.trim()"
                @click="sendMessage"
              >
                发送
              </el-button>
            </div>
          </el-card>
        </div>
      </el-main>
    </el-container>

    <!-- 创建频道对话框 -->
    <el-dialog
      v-model="createChannelDialogVisible"
      title="创建新频道"
      width="400px"
      destroy-on-close
      align-center
    >
      <el-form 
        :model="createChannelForm" 
        :rules="createChannelRules" 
        ref="createChannelFormRef"
        label-position="top"
      >
        <el-form-item label="频道名称" prop="name">
          <el-input 
            v-model="createChannelForm.name" 
            placeholder="请输入频道名称"
            maxlength="20"
            show-word-limit
            prefix-icon="ChatDotRound"
          />
        </el-form-item>
        <el-form-item label="频道简介">
          <el-input 
            v-model="createChannelForm.description" 
            type="textarea"
            placeholder="请输入频道简介"
            maxlength="200"
            show-word-limit
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createChannelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createChannel" :icon="Plus">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑频道对话框 -->
    <el-dialog
      v-model="editChannelDialogVisible"
      title="编辑频道"
      width="400px"
      destroy-on-close
      align-center
    >
      <el-form 
        :model="editChannelForm" 
        ref="editChannelFormRef"
        label-position="top"
      >
        <el-form-item label="频道名称">
          <el-input 
            v-model="editChannelForm.name" 
            placeholder="频道名称"
            disabled
            prefix-icon="ChatDotRound"
          />
          <el-text type="info" size="small">频道名称不可修改</el-text>
        </el-form-item>
        <el-form-item label="频道描述">
          <el-input 
            v-model="editChannelForm.description" 
            type="textarea"
            placeholder="请输入频道描述"
            :rows="3"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editChannelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateChannel" :icon="Edit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 个人信息对话框 -->
    <el-dialog
      v-model="userProfileDialogVisible"
      title="个人信息设置"
      width="500px"
      destroy-on-close
      align-center
    >
      <el-form 
        :model="userProfileForm" 
        :rules="userProfileRules" 
        ref="userProfileFormRef"
        label-position="top"
      >
        <div class="profile-header">
          <el-avatar :size="80" :src="user?.avatar">
            {{ user?.Username?.charAt(0).toUpperCase() }}
          </el-avatar>
          <el-button type="primary" link size="small">更换头像</el-button>
        </div>

        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userProfileForm.username" 
            placeholder="请输入用户名"
            maxlength="20"
            show-word-limit
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="电话号码" prop="phone">
          <el-input 
            v-model="userProfileForm.phone" 
            placeholder="请输入电话号码"
            disabled
            prefix-icon="Phone"
          />
        </el-form-item>

        <el-divider content-position="left">修改密码</el-divider>

        <el-form-item label="旧密码" prop="oldPassword">
          <el-input 
            v-model="userProfileForm.oldPassword" 
            type="password" 
            placeholder="请输入旧密码"
            show-password
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="userProfileForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
            prefix-icon="Key"
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="userProfileForm.confirmPassword" 
            type="password" 
            placeholder="请确认新密码"
            show-password
            prefix-icon="Check"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userProfileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateUserProfile" :icon="Check">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  ChatDotRound,
  ChatLineRound,
  ChatLineSquare,
  Document,
  Upload,
  Plus,
  Setting,
  SwitchButton,
  Search,
  User,
  More,
  Picture,
  Promotion,
  Download,
  Check,
  Lock,
  Key,
  Phone,
  CircleCheck,
  ChatSquare as AtIcon,
  Menu, CloseBold, PriceTag,
  Edit,
  Delete
} from '@element-plus/icons-vue'

export default {
  name: 'ChatRoom',
  computed: {
    ChatLineRound() {
      return ChatLineRound
    },
    ChatLineSquare() {
      return ChatLineSquare
    },
    Upload() {
      return Upload
    },
    Check() {
      return Check
    },
    Promotion() {
      return Promotion
    },
    PriceTag() {
      return PriceTag
    },
    Picture() {
      return Picture
    },
    Download() {
      return Download
    },
    Plus() {
      return Plus
    },
    Menu() {
      return Menu
    },
    CloseBold() {
      return CloseBold
    },
    More() {
      return More
    },
    Search() {
      return Search
    },
    User() {
      return User
    }
  },
  components: {
    ChatDotRound,
    ChatLineRound,
    ChatLineSquare,
    Document,
    Upload,
    Plus,
    Setting,
    SwitchButton,
    Search,
    User,
    More,
    Picture,
    Promotion,
    Download,
    Check,
    Lock,
    Key,
    Phone,
    CircleCheck,
    AtIcon,
    Menu,
    CloseBold,
    Edit,
    Delete
  },
  data() {
    return {
      user: null,
      token: '',
      messages: [],
      inputMessage: '',
      ws: null,
      currentChannel: 'general',
      privateChatUser: null,
      onlineUsers: [],
      channels: [],
      createChannelDialogVisible: false,
      userProfileDialogVisible: false,
      isSidebarOpen: false,
      createChannelForm: {
        name: '',
        description: ''
      },
      userProfileForm: {
        username: '',
        phone: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      currentTheme: 'light', // 默认浅色主题
      editChannelDialogVisible: false, // 编辑频道对话框
      editChannelForm: {
        name: '',
        description: ''
      },
      editingChannelId: null, // 正在编辑的频道ID
      pendingMessages: new Set(), // 跟踪待确认的消息，防止重复显示
      currentChannelDescription: '', // 当前频道的简介
      createChannelRules: {
        name: [
          { required: true, message: '请输入频道名称', trigger: 'blur' },
          { min: 2, max: 20, message: '频道名称长度在2到20个字符之间', trigger: 'blur' }
        ]
      },
      userProfileRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '用户名长度在2到20个字符之间', trigger: 'blur' }
        ],
        oldPassword: [
          { required: true, message: '请输入旧密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.userProfileForm.newPassword) {
                callback(new Error('两次输入的密码不一致'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  mounted() {
    const savedUser = localStorage.getItem('user')
    const savedToken = localStorage.getItem('token')
    if (savedUser && savedToken) {
      this.user = JSON.parse(savedUser)
      this.token = savedToken
      this.loadSavedTheme() // Load saved theme preference
      this.initWebSocket()
      this.initUserProfileForm()
      this.loadChannels() // Load channels when component mounts
      this.loadHistoryMessages() // Load history messages after WebSocket connection
    } else {
      this.$router.push({ name: 'Auth' })
    }
  },
  beforeUnmount() {
    if (this.ws) {
      this.ws.close()
    }
  },
  methods: {
    initUserProfileForm() {
      if (this.user) {
        this.userProfileForm = {
          username: this.user.Username || '',
          phone: this.user.Phone || '',
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
      }
    },
    initWebSocket() {
      const wsUrl = 'ws://localhost:8081/ws/chat'
      this.ws = new WebSocket(wsUrl)
      
      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
        this.sendLoginMessage()
      }
      
      this.ws.onmessage = (event) => {
        const message = JSON.parse(event.data)
        console.log('收到消息:', message)
        
        switch (message.type) {
          case 'loginSuccess':
            // After successful login, load history messages
            this.loadHistoryMessages()
            break
          case 'chat':
          case 'private':
          case 'group':
          case 'file':
          case 'privateFile':
          case 'groupFile':
            // 获取当前频道的ID
            const currentChannelObj = this.channels.find(c => c.name === this.currentChannel);
            const currentChannelId = currentChannelObj ? currentChannelObj.id : 0;
            
            // 检查是否是重复消息（防止发送的消息显示两次）
            if (message.tempId && this.pendingMessages.has(message.tempId)) {
              this.pendingMessages.delete(message.tempId);
              
              // 检查频道ID是否匹配当前频道
              if (message.channelId !== undefined && message.channelId !== currentChannelId) {
                // 不是当前频道的消息，不显示
                return;
              }
              
              // 添加消息到列表
              this.messages.push(message)
              this.scrollToBottom()
            } else if (!message.tempId) {
              // 服务器发送的消息，检查频道ID是否匹配
              // 如果消息有频道ID，检查是否匹配当前频道
              if (message.channelId !== undefined && message.channelId !== currentChannelId) {
                // 不是当前频道的消息，不显示
                return;
              }
              
              // 添加消息到列表
              this.messages.push(message)
              this.scrollToBottom()
            }
            break
          case 'userList':
            this.onlineUsers = message.users
            break
          default:
            console.log('未知消息类型:', message.type)
        }
      }
      
      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
        this.$message.error('连接出错，请检查网络')
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket连接关闭')
        this.$message.warning('连接已断开')
      }
    },
    sendLoginMessage() {
      if (this.ws.readyState === WebSocket.OPEN) {
        const loginMessage = {
          type: 'login',
          token: this.token
        }
        this.ws.send(JSON.stringify(loginMessage))
      }
    },
    sendMessage() {
      if (!this.inputMessage.trim() || !this.user) return
      
      // 生成临时消息ID，用于防止重复显示
      const tempMessageId = Date.now() + Math.random();
      this.pendingMessages.add(tempMessageId);
      
      // 获取当前频道的ID
      const currentChannelObj = this.channels.find(c => c.name === this.currentChannel);
      const channelId = currentChannelObj ? currentChannelObj.id : 0;
      
      const message = {
        type: this.privateChatUser ? 'private' : 'chat',
        senderId: this.user.ID,
        senderName: this.user.Username,
        targetUserId: this.privateChatUser?.ID,
        text: this.inputMessage,
        channelId: channelId, // 添加频道ID
        time: this.formatTime(new Date()),
        tempId: tempMessageId // 添加临时ID
      }
      
      if (this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify(message))
        this.inputMessage = ''
      } else {
        this.$message.error('连接未就绪，请稍后重试')
      }
    },
    handleEnter(e) {
      if (!e.shiftKey) {
        this.sendMessage()
      }
    },
    handleFileUpload(response) {
      if (response.success && this.user) {
        // 生成临时消息ID，用于防止重复显示
        const tempMessageId = Date.now() + Math.random();
        this.pendingMessages.add(tempMessageId);
        
        // 获取当前频道的ID
        const currentChannelObj = this.channels.find(c => c.name === this.currentChannel);
        const channelId = currentChannelObj ? currentChannelObj.id : 0;
        
        const fileMessage = {
          type: this.privateChatUser ? 'privateFile' : 'file',
          senderId: this.user.ID,
          senderName: this.user.Username,
          targetUserId: this.privateChatUser?.ID,
          fileName: response.fileName,
          fileUrl: response.fileUrl,
          channelId: channelId, // 添加频道ID
          time: this.formatTime(new Date()),
          tempId: tempMessageId
        }
        
        if (this.ws.readyState === WebSocket.OPEN) {
          this.ws.send(JSON.stringify(fileMessage))
        }
      }
    },
    switchChannel(channel) {
      this.currentChannel = channel
      this.privateChatUser = null
      // 更新当前频道的简介
      const currentChannelObj = this.channels.find(c => c.name === channel);
      this.currentChannelDescription = currentChannelObj?.description || '';
      // 不清空messages数组，让加载历史消息时自动替换
      this.loadChannelHistory()
    },
    openPrivateChat(onlineUser) {
      if (!this.user || onlineUser.ID === this.user.ID) return
      this.privateChatUser = onlineUser
      this.currentChannel = 'private'
      this.messages = []
      this.loadPrivateChatHistory()
    },
    getSenderName(message) {
      return message.senderName || message.user || '未知用户'
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const scrollbar = this.$refs.messageScrollbar
        if (scrollbar) {
          scrollbar.scrollTo({ top: scrollbar.wrapRef.scrollHeight, behavior: 'smooth' })
        }
      })
    },
    isDifferentDay(prevMsg, currMsg) {
      // 简单判断：如果时间字符串不同且包含"昨天"等字样，或者日期不同
      return prevMsg.time !== currMsg.time
    },
    formatTime(date) {
      if (!date) return '';
      
      // 如果已经是Date对象，直接使用
      if (!(date instanceof Date)) {
        date = new Date(date);
      }
      
      // 检查是否是有效的日期
      if (isNaN(date.getTime())) {
        return '';
      }
      
      // 使用中国时区格式化时间
      const options = {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
        timeZone: 'Asia/Shanghai'
      };
      
      return date.toLocaleTimeString('zh-CN', options);
    },
    showCreateChannelDialog() {
      this.createChannelForm.name = ''
      this.createChannelDialogVisible = true
    },
    async createChannel() {
      this.$refs.createChannelFormRef.validate(async (valid) => {
        if (valid) {
          try {
            // 调用后端API创建频道
            const response = await fetch('http://localhost:8081/api/channels/create', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({
              name: this.createChannelForm.name,
              creatorId: this.user.ID,
              description: this.createChannelForm.description
            })
            });

            if (response.ok) {
              const result = await response.json();
              if (result.code === 200) {
                // 添加到前端频道列表
                const newChannel = {
                  id: result.data, // 使用后端返回的ID
                  name: this.createChannelForm.name
                };
                this.channels.push(newChannel);
                this.createChannelDialogVisible = false;
                this.$message.success('频道创建成功');
                this.switchChannel(newChannel.name);
                
                // 重新加载频道列表以确保同步
                this.loadChannels();
              } else {
                this.$message.error(result.msg || '创建频道失败');
              }
            } else {
              this.$message.error('网络请求失败');
            }
          } catch (error) {
            console.error('创建频道失败:', error);
            this.$message.error('创建频道失败');
          }
        }
      })
    },
    
    showEditChannelDialog(channel) {
      this.editingChannelId = channel.id;
      this.editChannelForm.name = channel.name;
      this.editChannelForm.description = channel.description || '';
      this.editChannelDialogVisible = true;
    },
    
    async updateChannel() {
      if (!this.editingChannelId) return;
      
      try {
        const response = await fetch(`http://localhost:8081/api/channels/${this.editingChannelId}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: this.editChannelForm.name,
            description: this.editChannelForm.description,
            creatorId: this.user.ID
          })
        });

        if (response.ok) {
          const result = await response.json();
          if (result.code === 200) {
            this.editChannelDialogVisible = false;
            this.$message.success('频道更新成功');
            this.loadChannels();
          } else {
            this.$message.error(result.msg || '更新频道失败');
          }
        } else {
          this.$message.error('网络请求失败');
        }
      } catch (error) {
        console.error('更新频道失败:', error);
        this.$message.error('更新频道失败');
      }
    },
    
    async deleteChannel(channel) {
      if (channel.id === 0) {
        this.$message.warning('不能删除默认频道');
        return;
      }
      
      this.$confirm(`确定要删除频道 "${channel.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await fetch(`http://localhost:8081/api/channels/${channel.id}`, {
            method: 'DELETE'
          });

          if (response.ok) {
            const result = await response.json();
            if (result.code === 200) {
              this.$message.success('频道删除成功');
              
              // 如果当前频道是被删除的频道，切换到general频道
              if (this.currentChannel === channel.name) {
                this.switchChannel('general');
              }
              
              // 从频道列表中移除
              this.channels = this.channels.filter(c => c.id !== channel.id);
            } else {
              this.$message.error(result.msg || '删除频道失败');
            }
          } else {
            this.$message.error('网络请求失败');
          }
        } catch (error) {
          console.error('删除频道失败:', error);
          this.$message.error('删除频道失败');
        }
      }).catch(() => {});
    },
    
    // 加载频道列表
    async loadChannels() {
      try {
        const response = await fetch('http://localhost:8081/api/channels/all');
        if (response.ok) {
          const result = await response.json();
          if (result.code === 200) {
            // 过滤掉重复的频道，确保general频道只存在一次
            const apiChannels = result.data || [];
            const channelMap = new Map(); // 使用Map确保频道名称唯一
            
            // 先添加API返回的频道
            apiChannels.forEach(channel => {
              // 避免重复添加general频道
              if (channel.name.toLowerCase() !== 'general') {
                channelMap.set(channel.name, {
                  id: channel.id,
                  name: channel.name,
                  description: channel.description
                });
              }
            });
            
            // 确保general频道存在（且排在第一位）
            if (!channelMap.has('general')) {
              channelMap.set('general', { id: 0, name: 'general' });
            }
            
            // 转换为数组
            this.channels = Array.from(channelMap.values());
          }
        }
      } catch (error) {
        console.error('加载频道列表失败:', error);
      }
    },
    
    showUserProfile() {
      this.initUserProfileForm()
      this.userProfileDialogVisible = true
    },
    updateUserProfile() {
      this.$refs.userProfileFormRef.validate(async (valid) => {
        if (valid) {
          try {
            this.user.Username = this.userProfileForm.username
            localStorage.setItem('user', JSON.stringify(this.user))
            this.userProfileDialogVisible = false
            this.$message.success('个人信息更新成功')
          } catch (error) {
            console.error('更新个人信息失败:', error)
            this.$message.error('更新失败')
          }
        }
      })
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('user')
        localStorage.removeItem('token')
        this.$router.push({ name: 'Auth' })
        this.$message.success('已退出登录')
      }).catch(() => {})
    },
    toggleSidebar() {
      this.isSidebarOpen = !this.isSidebarOpen
    },
    
    toggleTheme() {
      this.currentTheme = this.currentTheme === 'light' ? 'dark' : 'light';
      this.applyTheme();
    },
    
    applyTheme() {
      // 根据当前主题设置CSS变量
      const root = document.documentElement;
      if (this.currentTheme === 'dark') {
        // 设置深色主题的颜色变量
        root.style.setProperty('--bg-primary', '#1a1d24');
        root.style.setProperty('--bg-secondary', '#2d3139');
        root.style.setProperty('--text-primary', '#ffffff');
        root.style.setProperty('--text-secondary', '#b0b0b0');
        root.style.setProperty('--border-color', '#444950');
        root.style.setProperty('--card-bg', '#23272d');
        root.style.setProperty('--hover-bg', '#3a3f48');
        document.body.classList.add('dark-theme');
      } else {
        // 恢复浅色主题的颜色变量
        root.style.setProperty('--bg-primary', '#f5f7fa');
        root.style.setProperty('--bg-secondary', '#ffffff');
        root.style.setProperty('--text-primary', '#303133');
        root.style.setProperty('--text-secondary', '#909399');
        root.style.setProperty('--border-color', '#dcdfe6');
        root.style.setProperty('--card-bg', '#ffffff');
        root.style.setProperty('--hover-bg', '#f5f7fa');
        document.body.classList.remove('dark-theme');
      }
      
      // 保存主题偏好到localStorage
      localStorage.setItem('chat-theme', this.currentTheme);
    },
    
    // 在组件挂载时应用保存的主题
    loadSavedTheme() {
      const savedTheme = localStorage.getItem('chat-theme');
      if (savedTheme) {
        this.currentTheme = savedTheme;
      }
      this.applyTheme();
    },
    closeSidebarOnMobile(event) {
      // Close sidebar on mobile when clicking outside of sidebar on mobile screens
      if (window.innerWidth <= 768 && this.isSidebarOpen) {
        const sidebar = document.querySelector('.sidebar');
        if (sidebar && !sidebar.contains(event.target)) {
          this.isSidebarOpen = false;
        }
      }
    },
    
    // 加载历史消息
    async loadHistoryMessages() {
      try {
        // Load general channel history
        const response = await fetch('http://localhost:8081/api/messages/channel/general')
        if (response.ok) {
          const result = await response.json()
          if (result.code === 200) {
            // Reverse to show oldest first, then append to current messages
            const historyMessages = result.data.reverse().map(msg => ({
              type: msg.messageType,
              senderId: msg.senderId,
              senderName: msg.senderName,
              text: msg.content,
              time: this.formatTime(new Date(msg.createTime)),
              fileUrl: msg.fileUrl,
              fileName: msg.fileName
            }))
            this.messages = [...historyMessages, ...this.messages]
            this.$nextTick(() => {
              this.scrollToBottom()
            })
          }
        }
      } catch (error) {
        console.error('加载历史消息失败:', error)
      }
    },
    
    // 加载特定频道的历史消息
    async loadChannelHistory() {
      try {
        let apiUrl = ''
        let channelId = null;
        
        if (this.currentChannel === 'general') {
          channelId = 0; // general频道的ID为0
        } else {
          const channel = this.channels.find(c => c.name === this.currentChannel);
          if (channel) {
            channelId = channel.id;
          } else {
            console.error('Channel not found');
            return;
          }
        }
        
        apiUrl = `http://localhost:8081/api/messages/channel/${channelId}`;
        
        const response = await fetch(apiUrl)
        if (response.ok) {
          const result = await response.json()
          if (result.code === 200) {
            // 直接替换消息列表，确保历史消息正确显示
            const historyMessages = result.data.reverse().map(msg => ({
              type: msg.messageType,
              senderId: msg.senderId,
              senderName: msg.senderName,
              text: msg.content,
              time: this.formatTime(new Date(msg.createTime)),
              fileUrl: msg.fileUrl,
              fileName: msg.fileName
            }))
            this.messages = historyMessages
            this.$nextTick(() => {
              this.scrollToBottom()
            })
          }
        }
      } catch (error) {
        console.error('加载频道历史消息失败:', error)
      }
    },
    
    // 加载私聊历史消息
    async loadPrivateChatHistory() {
      if (!this.privateChatUser) return
      
      try {
        const apiUrl = `http://localhost:8081/api/messages/private/${this.user.ID}/${this.privateChatUser.ID}`
        const response = await fetch(apiUrl)
        if (response.ok) {
          const result = await response.json()
          if (result.code === 200) {
            // Clear current messages and load private chat history
            const historyMessages = result.data.reverse().map(msg => ({
              type: msg.messageType,
              senderId: msg.senderId,
              senderName: msg.senderName,
              text: msg.content,
              time: this.formatTime(new Date(msg.createTime)),
              fileUrl: msg.fileUrl,
              fileName: msg.fileName
            }))
            this.messages = historyMessages
            this.$nextTick(() => {
              this.scrollToBottom()
            })
          }
        }
      } catch (error) {
        console.error('加载私聊历史消息失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.chat-app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
}

.chat-container {
  height: 100%;
  background-color: #f5f7fa;
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, #1a1d24 0%, #2d3139 100%);
  display: flex;
  flex-direction: column;
  border-right: 1px solid #2d3139;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-avatar {
  background: linear-gradient(135deg, #409EFF 0%, #1677ff 100%);
  color: white;
}

.brand-text {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.logout-btn {
  opacity: 0.8;
  transition: all 0.3s;
}

.logout-btn:hover {
  opacity: 1;
  transform: scale(1.05);
}

.section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 8px;
}

.section-title {
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.user-count :deep(.el-badge__content) {
  background-color: #67c23a;
}

/* 频道菜单样式 */
.channel-menu {
  background: transparent;
  border-right: none;
}

.channel-menu :deep(.el-menu-item) {
  color: #a8abb2;
  margin: 4px 0;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
}

.channel-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.05);
  color: #ffffff;
}

.channel-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.2) 0%, rgba(64, 158, 255, 0.05) 100%);
  color: #409EFF;
  border-left: 3px solid #409EFF;
}

.channel-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.channel-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.channel-menu :deep(.el-menu-item:hover) .channel-actions {
  opacity: 1;
}

.active-tag {
  margin-left: 8px;
  font-size: 10px;
}

/* 用户列表样式 */
.user-list {
  padding: 0 4px;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 4px;
  transition: all 0.3s;
  color: #b9bbbe;
}

.user-item:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  transform: translateX(4px);
}

.user-item.is-me {
  opacity: 0.7;
  cursor: default;
}

.user-item.is-me:hover {
  transform: none;
  background-color: transparent;
}

.user-status-badge :deep(.el-badge__content) {
  top: 6px;
  right: 6px;
  border: 2px solid #2d3139;
}

.user-info-text {
  margin-left: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  color: inherit;
  font-size: 14px;
  font-weight: 500;
}

.user-status {
  font-size: 12px;
  margin-top: 2px;
}

.chat-icon {
  opacity: 0;
  transition: opacity 0.3s;
  color: #409EFF;
}

.user-item:hover .chat-icon {
  opacity: 1;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 16px;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.user-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.user-card:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-badge :deep(.el-badge__content) {
  bottom: 4px;
  right: 4px;
  top: auto;
  border: 2px solid #2d3139;
}

.user-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-meta .user-name {
  color: #ffffff;
  font-weight: 500;
}

.settings-icon {
  color: #8c8d8f;
  font-size: 18px;
  transition: color 0.3s;
}

.user-card:hover .settings-icon {
  color: #409EFF;
  animation: rotate 0.5s ease;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(90deg); }
}

/* 主聊天区域 */
.chat-main {
  display: flex;
  flex-direction: column;
  padding: 0;
  background-color: #ffffff;
  position: relative;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.channel-icon {
  padding: 8px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 12px;
}

.header-info {
  display: flex;
  flex-direction: column;
}

.channel-name {
  font-size: 16px;
  color: #303133;
}

.channel-desc {
  margin-top: 2px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 消息容器 */
.message-container {
  flex: 1;
  overflow: hidden;
  position: relative;
  background: #f5f7fa;
}

.message-list {
  height: 100%;
  padding: 20px 24px;
}

.messages-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 消息项样式 */
.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-content-wrapper {
  display: flex;
  max-width: 70%;
  gap: 12px;
}

.own-message {
  justify-content: flex-end;
}

.own-message .message-content-wrapper {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.own-message .message-body {
  align-items: flex-end;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;
}

.own-message .message-header {
  flex-direction: row-reverse;
}

.sender-name {
  color: #303133;
  font-size: 14px;
}

.message-time {
  font-size: 12px;
}

/* 消息气泡 */
.message-bubble {
  background: #ffffff;
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  max-width: 100%;
  word-break: break-word;
}

.own-bubble {
  background: linear-gradient(135deg, #409EFF 0%, #1677ff 100%);
  color: #ffffff;
}

.own-bubble :deep(.el-text) {
  color: #ffffff !important;
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}

/* 文件卡片 */
.file-card {
  width: 320px;
  background: #ffffff;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.file-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.file-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  width: 48px;
  height: 48px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #303133;
}

/* 时间分割线 */
.time-divider {
  margin: 16px 0;
}

.time-divider :deep(.el-divider__text) {
  background: #f5f7fa;
  padding: 0 16px;
}

/* 输入区域 */
.chat-input-area {
  padding: 16px 24px 24px;
  background: #ffffff;
  border-top: 1px solid #e4e7ed;
}

.input-card {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 16px;
  transition: all 0.3s;
}

.input-card:focus-within {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.input-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-bottom: 1px solid #e4e7ed;
}

.upload-btn {
  display: inline-flex;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  padding: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  background: #ffffff;
  border: none;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
  resize: none;
  box-shadow: none;
}

.message-input :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.send-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 12px;
  font-weight: 500;
}

.send-btn.is-disabled {
  opacity: 0.6;
}

/* 空状态 */
:deep(.el-empty) {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 对话框样式优化 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: linear-gradient(90deg, #f5f7fa 0%, #ffffff 100%);
}

:deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
  background: #f5f7fa;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px dashed #dcdfe6;
}

/* 滚动条美化 */
:deep(.el-scrollbar__bar) {
  opacity: 0.3;
  transition: opacity 0.3s;
}

:deep(.el-scrollbar__bar:hover) {
  opacity: 0.6;
}

:deep(.el-scrollbar__thumb) {
  background: #c0c4cc;
  border-radius: 4px;
}

/* 响应式适配 */
@media (max-width: 1199px) {
  .chat-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100% !important;
    height: auto !important;
    max-height: 200px;
  }
  
  .sidebar-content {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .section {
    flex: 1;
    min-width: 200px;
  }
  
  .chat-main {
    height: calc(100vh - 200px) !important;
  }
  
  .sidebar-footer {
    width: 100%;
    border-top: 1px solid #e6e6e6;
    margin-top: 12px;
    padding-top: 12px;
  }
}

@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100% !important;
    height: auto !important;
    max-height: 220px;
    position: fixed;
    top: 0;
    left: -100%;
    z-index: 1000;
    background: white;
    padding: 16px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    transition: left 0.3s ease;
  }
  
  .sidebar.open {
    left: 0;
  }
  
  .sidebar-content {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .section {
    flex: 1;
    min-width: 160px;
    margin-bottom: 0;
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 8px;
  }
  
  .channel-menu {
    max-height: 100px;
  }
  
  .user-list {
    max-height: 100px !important;
  }
  
  .chat-main {
    height: 100vh !important;
    padding-top: 60px;
  }
  
  .chat-header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 999;
    background: white;
    border-bottom: 1px solid #e6e6e6;
  }
  
  .message-container {
    margin-top: 60px;
    margin-bottom: 150px;
  }
  
  .chat-input-area {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 998;
    background: white;
    border-top: 1px solid #e6e6e6;
    padding: 8px 12px;
  }
  
  .input-card {
    border-radius: 0;
    border: none;
    box-shadow: none;
  }
  
  .input-toolbar {
    margin-bottom: 8px;
  }
  
  .input-wrapper {
    display: flex;
  }
  
  .message-input {
    flex: 1;
    margin-right: 8px;
  }
  
  .send-btn {
    height: 40px;
    min-width: 60px;
  }
  
  .sidebar-footer {
    width: 100%;
    border-top: 1px solid #e6e6e6;
    margin-top: 12px;
    padding-top: 12px;
  }
  
  .user-profile {
    flex: 1;
  }
  
  .header-actions {
    display: none;
  }
  
  .brand {
    display: none;
  }
  
  .logout-btn {
    display: block;
    position: fixed;
    top: 16px;
    left: 16px;
    z-index: 1001;
  }
}

@media (max-width: 576px) {
  .section {
    min-width: 140px;
  }
  
  .user-item {
    padding: 8px;
  }
  
  .user-info-text {
    max-width: calc(100% - 60px);
  }
  
  .user-name {
    font-size: 13px;
  }
  
  .message-item {
    padding: 8px 12px;
  }
  
  .message-avatar {
    width: 32px !important;
    height: 32px !important;
  }
  
  .sender-name {
    font-size: 14px;
  }
  
  .message-time {
    font-size: 11px;
  }
  
  .message-bubble {
    padding: 8px 12px !important;
  }
  
  .message-text {
    font-size: 14px;
  }
  
  .file-card {
    margin: 8px 0;
  }
  
  .file-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .file-info {
    width: 100%;
  }
  
  .input-card {
    padding: 8px;
  }
  
  .input-toolbar {
    order: 2;
    margin-top: 8px;
    margin-bottom: 0;
    justify-content: center;
  }
  
  .input-wrapper {
    order: 1;
  }
  
  .message-input {
    margin-right: 0;
    margin-bottom: 8px;
  }
  
  .send-btn {
    width: 100%;
    height: 44px;
  }
}

/* Mobile sidebar toggle button */
.mobile-sidebar-toggle {
  display: none;
  position: fixed;
  top: 16px;
  left: 16px;
  z-index: 1001;
  background: var(--el-color-primary);
  color: white;
}

@media (max-width: 768px) {
  .mobile-sidebar-toggle {
    display: block;
  }
}
  
  /* 深色主题样式 */
  .dark-theme {
    --bg-primary: #1a1d24;
    --bg-secondary: #2d3139;
    --text-primary: #ffffff;
    --text-secondary: #b0b0b0;
    --border-color: #444950;
    --card-bg: #23272d;
    --hover-bg: #3a3f48;
  }
  
  .dark-theme .chat-container {
    background-color: var(--bg-primary);
  }
  
  .dark-theme .sidebar {
    background: linear-gradient(180deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
  }
  
  .dark-theme .sidebar-item {
    color: var(--text-secondary);
  }
  
  .dark-theme .sidebar-item:hover {
    background-color: var(--hover-bg);
  }
  
  .dark-theme .sidebar-item.active {
    background-color: #409EFF;
    color: white;
  }
  
  .dark-theme .chat-header {
    background-color: var(--bg-secondary);
    border-bottom: 1px solid var(--border-color);
    color: var(--text-primary);
  }
  
  .dark-theme .channel-name {
    color: var(--text-primary);
  }
  
  .dark-theme .message-list {
    background-color: var(--bg-primary);
  }
  
  .dark-theme .message-item {
    background-color: var(--card-bg);
    border: 1px solid var(--border-color);
  }
  
  .dark-theme .sender-name {
    color: var(--text-primary);
  }
  
  .dark-theme .message-time {
    color: var(--text-secondary);
  }
  
  .dark-theme .message-content {
    color: var(--text-primary);
  }
  
  .dark-theme .chat-input-area {
    background-color: var(--bg-secondary);
    border-top: 1px solid var(--border-color);
  }
  
  .dark-theme .el-input__wrapper {
    background-color: var(--card-bg) !important;
    box-shadow: 0 0 0 1px var(--border-color) inset;
  }
  
  .dark-theme .el-input__inner {
    background-color: var(--card-bg) !important;
    color: var(--text-primary) !important;
    border-color: var(--border-color) !important;
  }
  
  .dark-theme .el-textarea__inner {
    background-color: var(--card-bg) !important;
    color: var(--text-primary) !important;
    border-color: var(--border-color) !important;
  }
  
  .dark-theme .el-input__wrapper:hover {
    box-shadow: 0 0 0 1px var(--hover-bg) inset;
  }
  
  .dark-theme .el-input__wrapper.is-focus {
    box-shadow: 0 0 0 1px #409EFF inset;
  }
  
  .dark-theme .el-button {
    --el-button-bg-color: var(--card-bg);
    --el-button-border-color: var(--border-color);
    --el-button-text-color: var(--text-primary);
  }
  
  .dark-theme .el-card {
    --el-card-bg-color: var(--card-bg);
  }
  
  .dark-theme .file-card {
    background-color: var(--card-bg);
    border: 1px solid var(--border-color);
  }
  
  .dark-theme .el-empty {
    background-color: var(--bg-primary);
  }
  
  .dark-theme .el-divider__text {
    background-color: var(--bg-primary);
  }
  
  .dark-theme .time-divider :deep(.el-divider__text) {
    background-color: var(--bg-primary);
  }
  
  .dark-theme .channel-icon {
    color: #409EFF;
  }
</style>