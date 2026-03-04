<template>
  <div class="chat-app" :class="{ 'dark-theme': currentTheme === 'dark' }" @click="closeSidebarOnMobile">
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
              <!-- 固定的综合频道：index 必须是 general -->
              <el-menu-item index="general">
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
                    <div class="channel-actions">
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
                <el-badge
                    is-dot
                    :type="onlineUser.ID === user?.ID ? 'success' : 'primary'"
                    class="user-status-badge"
                >
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
                {{
                  privateChatUser
                      ? privateChatUser.Username
                      : (currentChannel === 'general' ? '综合频道' : currentChannel)
                }}
              </el-text>
              <el-text type="info" size="small" class="channel-desc">
                {{
                  privateChatUser
                      ? '私聊中'
                      : (currentChannel === 'general'
                          ? '所有人都可以在此聊天'
                          : (currentChannelDescription || '频道聊天'))
                }}
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
            <el-dropdown placement="bottom-end" trigger="click" @command="handleDropdownCommand">
              <el-button :icon="More" circle />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="theme">
                    <el-icon><SwitchButton /></el-icon>
                    {{ currentTheme === 'dark' ? '切换到浅色模式' : '切换到深色模式' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="logout">
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
                  :key="message._key || index"
                  class="message-item"
                  :class="{
                  'own-message': user && message.senderId === user.ID,
                  'has-file': message.fileUrl
                }"
              >
                <!-- 日期分割线 -->
                <div
                    v-if="index === 0 || isDifferentDay(messages[index - 1], message)"
                    class="time-divider"
                >
                  <el-divider>
                    <el-text type="info" size="small">
                      {{ formatDateDivider(getMessageTs(message)) }}
                    </el-text>
                  </el-divider>
                </div>

                <div class="message-content-wrapper">
                  <el-avatar :size="40" :src="message.avatar" class="message-avatar">
                    {{ getSenderName(message).charAt(0).toUpperCase() }}
                  </el-avatar>

                  <div class="message-body">
                    <div class="message-header">
                      <el-text class="sender-name" tag="b">{{ getSenderName(message) }}</el-text>
                      <el-text type="info" size="small" class="message-time">
                        {{ formatClock(getMessageTs(message)) }}
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
                    :action="`${API_BASE_URL}/api/files/upload`"
                    :on-success="handleFileUpload"
                    :show-file-list="false"
                    accept="*"
                >
                  <el-button :icon="Upload" link />
                </el-upload>
              </el-tooltip>

              <el-divider direction="vertical" />

              <!-- ✅ 表情：用 el-popover（点击触发 + 自动处理点击外部关闭） -->
              <el-popover
                  v-model:visible="showEmojiPicker"
                  trigger="click"
                  placement="top-start"
                  :width="320"
                  popper-class="emoji-popper"
              >
                <template #reference>
                  <el-tooltip content="表情">
                    <el-button :icon="Picture" link />
                  </el-tooltip>
                </template>

                <div class="emoji-grid">
                  <span
                      v-for="emoji in emojis"
                      :key="emoji"
                      class="emoji-item"
                      @click="insertEmoji(emoji)"
                  >
                    {{ emoji }}
                  </span>
                </div>
              </el-popover>

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
      <el-form :model="editChannelForm" ref="editChannelFormRef" label-position="top">
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
import { API_BASE_URL, WS_BASE_URL } from '../config.js'
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
    ChatLineRound() { return ChatLineRound },
    ChatLineSquare() { return ChatLineSquare },
    Upload() { return Upload },
    Check() { return Check },
    Promotion() { return Promotion },
    PriceTag() { return PriceTag },
    Picture() { return Picture },
    Download() { return Download },
    Plus() { return Plus },
    Menu() { return Menu },
    CloseBold() { return CloseBold },
    More() { return More },
    Search() { return Search },
    User() { return User }
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

      createChannelForm: { name: '', description: '' },
      userProfileForm: { username: '', phone: '', oldPassword: '', newPassword: '', confirmPassword: '' },

      currentTheme: 'light',

      editChannelDialogVisible: false,
      editChannelForm: { name: '', description: '' },
      editingChannelId: null,

      pendingMessages: new Set(),
      currentChannelDescription: '',

      // ✅ 用 Popover 的 visible 做开关
      showEmojiPicker: false,
      emojis: [
        '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
        '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩',
        '😘', '😗', '😚', '😋', '😛', '😜', '🤪', '😝',
        '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐',
        '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌',
        '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢',
        '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠',
        '🥳', '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️',
        '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨',
        '😰', '😥', '😢', '😭', '😱', '😖', '😣', '😞',
        '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬',
        '😈', '👿', '💀', '☠️', '💩', '🤡', '👹', '👺',
        '👻', '👽', '👾', '🤖', '😺', '😸', '😹', '😻',
        '😼', '😽', '🙀', '😿', '😾', '❤️', '🧡', '💛',
        '💚', '💙', '💜', '🖤', '💔', '❣️', '💕', '💞',
        '💓', '💗', '💖', '💘', '💝', '💟', '👍', '👎'
      ],

      createChannelRules: {
        name: [
          { required: true, message: '请输入频道名称', trigger: 'blur' },
          { min: 1, max: 20, message: '频道名称长度在1到20个字符之间', trigger: 'blur' }
        ]
      },
      userProfileRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '用户名长度在2到20个字符之间', trigger: 'blur' }
        ],
        oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (value !== this.userProfileForm.newPassword) callback(new Error('两次输入的密码不一致'))
              else callback()
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
      this.user.avatar = this.getAvatar(this.user.ID)
      localStorage.setItem('user', JSON.stringify(this.user))
      this.token = savedToken
      this.loadSavedTheme()
      this.initUserProfileForm()
      this.loadChannels()
      this.initWebSocket()
    } else {
      this.$router.push({ name: 'Auth' })
    }
  },
  beforeUnmount() {
    if (this.ws) this.ws.close()
  },
  methods: {
    getAvatar(userId) {
      if (!userId) return this.generateRandomAvatar()
      
      const avatarCacheKey = 'avatar_cache'
      const cache = JSON.parse(localStorage.getItem(avatarCacheKey) || '{}')
      
      if (cache[userId]) {
        return cache[userId]
      }
      
      const newAvatar = this.generateRandomAvatar()
      cache[userId] = newAvatar
      localStorage.setItem(avatarCacheKey, JSON.stringify(cache))
      
      return newAvatar
    },
    generateRandomAvatar() {
      const timestamp = Date.now()
      return `https://api.manhuaidc.cn/API/miao.php?t=${timestamp}`
    },
    // ---------- 时间与分割线 ----------
    getMessageTs(message) {
      if (typeof message?._ts === 'number') return message._ts
      if (typeof message?.timestamp === 'number') return message.timestamp
      if (message?.createTime) {
        const t = new Date(message.createTime).getTime()
        return Number.isFinite(t) ? t : Date.now()
      }
      if (message?.time) {
        const t = new Date(message.time).getTime()
        return Number.isFinite(t) ? t : Date.now()
      }
      return Date.now()
    },
    formatClock(ts) {
      const d = new Date(ts)
      if (isNaN(d.getTime())) return ''
      return d.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
        timeZone: 'Asia/Shanghai'
      })
    },
    formatDateDivider(ts) {
      const d = new Date(ts)
      if (isNaN(d.getTime())) return ''
      return d.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        timeZone: 'Asia/Shanghai'
      })
    },
    isDifferentDay(prevMsg, currMsg) {
      const a = new Date(this.getMessageTs(prevMsg))
      const b = new Date(this.getMessageTs(currMsg))
      return (
          a.getFullYear() !== b.getFullYear() ||
          a.getMonth() !== b.getMonth() ||
          a.getDate() !== b.getDate()
      )
    },

    // ---------- 基础 ----------
    initUserProfileForm() {
      if (!this.user) return
      this.userProfileForm = {
        username: this.user.Username || '',
        phone: this.user.Phone || '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    },

    getCurrentChannelId() {
      if (this.privateChatUser) return null
      if (this.currentChannel === 'general') return 0
      const ch = this.channels.find(c => c.name === this.currentChannel)
      return ch ? ch.id : null
    },

    normalizeIncomingMessage(message) {
      const ts = this.getMessageTs(message)
      return {
        ...message,
        avatar: this.getAvatar(message.senderId),
        _ts: ts,
        _key: message.tempId || message.id || `${message.senderId || 'u'}-${ts}-${Math.random()}`
      }
    },

    // ---------- WebSocket ----------
    initWebSocket() {
      const wsUrl = `${WS_BASE_URL}/ws/chat`
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
        this.sendLoginMessage()
      }

      this.ws.onmessage = (event) => {
        const raw = JSON.parse(event.data)
        const message = this.normalizeIncomingMessage(raw)

        switch (message.type) {
          case 'loginSuccess':
            this.loadChannelHistory()
            break

          case 'chat':
          case 'private':
          case 'group':
          case 'file':
          case 'privateFile':
          case 'groupFile': {
            const currentChannelId = this.getCurrentChannelId()
            if (message.channelId !== undefined && currentChannelId !== null) {
              if (message.channelId !== currentChannelId) return
            }

            if (message.tempId && this.pendingMessages.has(message.tempId)) {
              this.pendingMessages.delete(message.tempId)
              this.messages.push(message)
              this.scrollToBottom()
              return
            }

            if (!message.tempId) {
              this.messages.push(message)
              this.scrollToBottom()
            }
            break
          }

          case 'userList':
            this.onlineUsers = (message.users || []).map(user => ({
              ...user,
              avatar: this.getAvatar(user.ID)
            }))
            break
            
          case 'usernameUpdated': {
            // Update user list with new username
            this.onlineUsers = this.onlineUsers.map(user => 
              user.ID === message.userId 
                ? { ...user, Username: message.newUsername } 
                : user
            )
            break
          }

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
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'login', token: this.token }))
      }
    },

    // ---------- 发送消息 / 文件 ----------
    sendMessage() {
      if (!this.inputMessage.trim() || !this.user) return

      const tempMessageId = Date.now() + Math.random()
      this.pendingMessages.add(tempMessageId)

      const channelId = this.getCurrentChannelId() ?? 0

      const message = {
        type: this.privateChatUser ? 'private' : 'chat',
        senderId: this.user.ID,
        senderName: this.user.Username,
        targetUserId: this.privateChatUser?.ID,
        text: this.inputMessage,
        channelId,
        time: new Date().toISOString(),
        timestamp: Date.now(),
        tempId: tempMessageId
      }

      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify(message))
        this.inputMessage = ''
      } else {
        this.$message.error('连接未就绪，请稍后重试')
      }
    },

    handleEnter(e) {
      if (!e.shiftKey) this.sendMessage()
    },

    handleFileUpload(response) {
      if (!response?.success || !this.user) return

      const tempMessageId = Date.now() + Math.random()
      this.pendingMessages.add(tempMessageId)

      const channelId = this.getCurrentChannelId() ?? 0

      const fileMessage = {
        type: this.privateChatUser ? 'privateFile' : 'file',
        senderId: this.user.ID,
        senderName: this.user.Username,
        targetUserId: this.privateChatUser?.ID,
        fileName: response.fileName,
        fileUrl: response.fileUrl,
        channelId,
        time: new Date().toISOString(),
        timestamp: Date.now(),
        tempId: tempMessageId
      }

      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify(fileMessage))
      }
    },

    // ---------- 切换频道 / 私聊 ----------
    switchChannel(channelName) {
      // Notify backend that user is exiting private chat
      if (this.privateChatUser && this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({
          type: 'exitPrivateChat'
        }))
      }
      
      this.currentChannel = channelName
      this.privateChatUser = null

      if (channelName === 'general') {
        this.currentChannelDescription = '所有人都可以在此聊天'
      } else {
        const currentChannelObj = this.channels.find(c => c.name === channelName)
        this.currentChannelDescription = currentChannelObj?.description || ''
      }

      this.loadChannelHistory()
    },

    openPrivateChat(onlineUser) {
      if (!this.user || onlineUser.ID === this.user.ID) return
      this.privateChatUser = {
        ...onlineUser,
        avatar: this.getAvatar(onlineUser.ID)
      }
      this.currentChannel = 'private'
      this.messages = []
      this.loadPrivateChatHistory()
      
      // Notify backend that user is in private chat
      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({
          type: 'enterPrivateChat',
          targetUserId: onlineUser.ID
        }))
      }
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

    // ---------- ✅ 表情插入 ----------
    insertEmoji(emoji) {
      this.inputMessage += emoji
      // 选中后关闭 Popover
      this.showEmojiPicker = false
    },

    // ---------- 频道 CRUD ----------
    showCreateChannelDialog() {
      this.createChannelForm.name = ''
      this.createChannelForm.description = ''
      this.createChannelDialogVisible = true
    },

    async createChannel() {
      this.$refs.createChannelFormRef.validate(async (valid) => {
        if (!valid) return
        try {
          const response = await fetch(`${API_BASE_URL}/api/channels/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              name: this.createChannelForm.name,
              creatorId: this.user.ID,
              description: this.createChannelForm.description
            })
          })

          if (!response.ok) return this.$message.error('网络请求失败')

          const result = await response.json()
          if (result.code === 200) {
            this.createChannelDialogVisible = false
            this.$message.success('频道创建成功')
            await this.loadChannels()
            this.switchChannel(this.createChannelForm.name)
          } else {
            this.$message.error(result.msg || '创建频道失败')
          }
        } catch (error) {
          console.error('创建频道失败:', error)
          this.$message.error('创建频道失败')
        }
      })
    },

    showEditChannelDialog(channel) {
      this.editingChannelId = channel.id
      this.editChannelForm.name = channel.name
      this.editChannelForm.description = channel.description || ''
      this.editChannelDialogVisible = true
    },

    async updateChannel() {
      if (!this.editingChannelId) return
      try {
        const response = await fetch(`${API_BASE_URL}/api/channels/${this.editingChannelId}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            name: this.editChannelForm.name,
            description: this.editChannelForm.description,
            creatorId: this.user.ID
          })
        })

        if (!response.ok) return this.$message.error('网络请求失败')

        const result = await response.json()
        if (result.code === 200) {
          this.editChannelDialogVisible = false
          this.$message.success('频道更新成功')
          await this.loadChannels()

          const current = this.channels.find(c => c.name === this.currentChannel)
          this.currentChannelDescription = current?.description || this.currentChannelDescription
        } else {
          this.$message.error(result.msg || '更新频道失败')
        }
      } catch (error) {
        console.error('更新频道失败:', error)
        this.$message.error('更新频道失败')
      }
    },

    async deleteChannel(channel) {
      this.$confirm(`确定要删除频道 "${channel.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await fetch(`${API_BASE_URL}/api/channels/${channel.id}`, {
            method: 'DELETE'
          })

          if (!response.ok) return this.$message.error('网络请求失败')

          const result = await response.json()
          if (result.code === 200) {
            this.$message.success('频道删除成功')
            if (this.currentChannel === channel.name) this.switchChannel('general')
            await this.loadChannels()
          } else {
            this.$message.error(result.msg || '删除频道失败')
          }
        } catch (error) {
          console.error('删除频道失败:', error)
          this.$message.error('删除频道失败')
        }
      }).catch(() => {})
    },

    async loadChannels() {
      try {
        const response = await fetch(`${API_BASE_URL}/api/channels/all`)
        if (!response.ok) return
        const result = await response.json()
        if (result.code !== 200) return

        const apiChannels = result.data || []

        this.channels = apiChannels
            .filter(c => String(c.name || '').toLowerCase() !== 'general')
            .map(c => ({
              id: c.id,
              name: c.name,
              description: c.description
            }))
      } catch (error) {
        console.error('加载频道列表失败:', error)
      }
    },

    // ---------- 个人信息 ----------
    showUserProfile() {
      this.initUserProfileForm()
      this.userProfileDialogVisible = true
    },

    updateUserProfile() {
      this.$refs.userProfileFormRef.validate(async (valid) => {
        if (!valid) return
        try {
          // 1. 更新本地状态
          const oldUsername = this.user.Username
          this.user.Username = this.userProfileForm.username
          
          // 2. 更新 WebSocket 连接中的用户信息
          if (this.ws?.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({ 
              type: 'updateUsername',
              userId: this.user.ID,
              oldUsername: oldUsername,
              newUsername: this.userProfileForm.username
            }))
          }
          
          // 3. 保存到 localStorage
          localStorage.setItem('user', JSON.stringify(this.user))
          
          // 4. 关闭对话框
          this.userProfileDialogVisible = false
          this.$message.success('个人信息更新成功')
        } catch (error) {
          console.error('更新个人信息失败:', error)
          this.$message.error('更新失败')
        }
      })
    },

    handleDropdownCommand(command) {
      if (command === 'theme') {
        this.toggleTheme()
      } else if (command === 'logout') {
        this.handleLogout()
      }
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

    // ---------- UI ----------
    toggleSidebar() {
      this.isSidebarOpen = !this.isSidebarOpen
    },

    toggleTheme() {
      this.currentTheme = this.currentTheme === 'light' ? 'dark' : 'light'
      this.applyTheme()
    },

    applyTheme() {
      const root = document.documentElement
      if (this.currentTheme === 'dark') {
        root.style.setProperty('--bg-primary', '#1a1d24')
        root.style.setProperty('--bg-secondary', '#2d3139')
        root.style.setProperty('--text-primary', '#ffffff')
        root.style.setProperty('--text-secondary', '#b0b0b0')
        root.style.setProperty('--border-color', '#444950')
        root.style.setProperty('--card-bg', '#23272d')
        root.style.setProperty('--hover-bg', '#3a3f48')
      } else {
        root.style.setProperty('--bg-primary', '#f5f7fa')
        root.style.setProperty('--bg-secondary', '#ffffff')
        root.style.setProperty('--text-primary', '#303133')
        root.style.setProperty('--text-secondary', '#909399')
        root.style.setProperty('--border-color', '#dcdfe6')
        root.style.setProperty('--card-bg', '#ffffff')
        root.style.setProperty('--hover-bg', '#f5f7fa')
      }
      localStorage.setItem('chat-theme', this.currentTheme)
    },

    loadSavedTheme() {
      const savedTheme = localStorage.getItem('chat-theme')
      if (savedTheme) this.currentTheme = savedTheme
      this.applyTheme()
    },

    closeSidebarOnMobile(event) {
      if (window.innerWidth <= 768 && this.isSidebarOpen) {
        const sidebar = document.querySelector('.sidebar')
        if (sidebar && !sidebar.contains(event.target)) {
          this.isSidebarOpen = false
        }
      }
    },

    // ---------- 历史消息 ----------
    async loadChannelHistory() {
      try {
        if (this.privateChatUser) return

        const channelId = this.getCurrentChannelId()
        if (channelId === null) return

        const apiUrl = `${API_BASE_URL}/api/messages/channel/${channelId}`
        const response = await fetch(apiUrl)
        if (!response.ok) return

        const result = await response.json()
        if (result.code !== 200) return

        const historyMessages = (result.data || []).slice().reverse().map(msg => this.normalizeIncomingMessage({
          type: msg.messageType,
          senderId: msg.senderId,
          senderName: msg.senderName,
          text: msg.content,
          createTime: msg.createTime,
          fileUrl: msg.fileUrl,
          fileName: msg.fileName
        }))

        this.messages = historyMessages
        this.$nextTick(() => this.scrollToBottom())
      } catch (error) {
        console.error('加载频道历史消息失败:', error)
      }
    },

    async loadPrivateChatHistory() {
      if (!this.privateChatUser) return
      try {
        const apiUrl = `${API_BASE_URL}/api/messages/private/${this.user.ID}/${this.privateChatUser.ID}`
        const response = await fetch(apiUrl)
        if (!response.ok) return

        const result = await response.json()
        if (result.code !== 200) return

        const historyMessages = (result.data || []).slice().reverse().map(msg => this.normalizeIncomingMessage({
          type: msg.messageType,
          senderId: msg.senderId,
          senderName: msg.senderName,
          text: msg.content,
          createTime: msg.createTime,
          fileUrl: msg.fileUrl,
          fileName: msg.fileName
        }))

        this.messages = historyMessages
        this.$nextTick(() => this.scrollToBottom())
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
  background-color: var(--bg-primary, #f5f7fa);
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
  background-color: var(--bg-secondary, #ffffff);
  position: relative;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: var(--bg-secondary, #ffffff);
  border-bottom: 1px solid var(--border-color, #e4e7ed);
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
  color: var(--text-primary, #303133);
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
  background-color: var(--bg-secondary, #ffffff);
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
  color: var(--text-primary, #303133);
  font-size: 14px;
}

.message-time {
  font-size: 12px;
}

/* 消息气泡 */
.message-bubble {
  background: var(--bg-secondary, #ffffff);
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
  color: var(--text-primary, #303133);
}

/* 文件卡片 */
.file-card {
  width: 320px;
  background: var(--bg-secondary, #ffffff);
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
  color: var(--text-primary, #303133);
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
  background: var(--bg-secondary, #ffffff);
  border-top: 1px solid var(--border-color, #e4e7ed);
}

.input-card {
  background: var(--bg-primary, #f5f7fa);
  border: 1px solid var(--border-color, #e4e7ed);
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

/* ✅ Popover 内容样式：Popover Teleport 到 body，scoped 需要 :deep */
:deep(.emoji-popper) {
  border-radius: 12px;
  padding: 12px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  max-width: 320px;
}

.emoji-item {
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.emoji-item:hover {
  background: #f5f7fa;
  transform: scale(1.2);
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
  background: var(--bg-secondary, #ffffff);
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
  .mobile-sidebar-toggle { display: block; }
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

.dark-theme :deep(.emoji-popper) {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
}

.dark-theme .emoji-item:hover {
  background: var(--hover-bg);
}

.dark-theme :deep(.el-input__wrapper),
.dark-theme :deep(.el-textarea__inner) {
  background-color: var(--bg-primary) !important;
  box-shadow: 0 0 0 1px var(--border-color) inset !important;
}

.dark-theme :deep(.el-input__inner),
.dark-theme :deep(.el-textarea__inner) {
  color: var(--text-primary) !important;
}

.dark-theme :deep(.el-menu) {
  background-color: transparent !important;
  border-right: none !important;
}

.dark-theme :deep(.el-menu-item),
.dark-theme :deep(.el-menu-item span) {
  color: var(--text-secondary) !important;
}

.dark-theme :deep(.el-menu-item.is-active) {
  background-color: var(--hover-bg) !important;
  color: var(--text-primary) !important;
}

.dark-theme :deep(.el-divider) {
  border-color: var(--border-color);
}

.dark-theme :deep(.el-button) {
  color: var(--text-primary);
}
</style>
