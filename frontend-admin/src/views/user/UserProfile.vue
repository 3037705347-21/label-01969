<template>
  <div class="user-profile">
    <div class="page-card">
      <h2 class="page-title">个人中心</h2>
      
      <el-row :gutter="32">
        <el-col :span="8">
          <div class="profile-card">
            <el-avatar :size="100" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <h3>{{ userStore.userInfo?.username }}</h3>
            <el-tag>{{ userStore.userInfo?.roleName }}</el-tag>
            <el-tag :type="userStore.userInfo?.verifyStatus === 1 ? 'success' : 'info'" style="margin-left: 8px">
              {{ userStore.userInfo?.verifyStatus === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </div>
        </el-col>
        
        <el-col :span="16">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="basic">
              <el-form ref="basicFormRef" :model="basicForm" label-width="100px">
                <el-form-item label="真实姓名">
                  <el-input v-model="basicForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="basicForm.phone" disabled />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="basicForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="地址">
                  <el-input v-model="basicForm.address" placeholder="请输入地址" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="saveLoading" @click="handleSaveBasic">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="实名认证" name="verify" v-if="userStore.isAdopter || userStore.isRescuer">
              <el-form ref="verifyFormRef" :model="verifyForm" :rules="verifyRules" label-width="100px">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="verifyForm.realName" placeholder="请输入真实姓名" maxlength="20" />
                </el-form-item>
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="verifyForm.idCard" placeholder="请输入身份证号" maxlength="18" />
                </el-form-item>
                <el-form-item label="居住地址" prop="address">
                  <el-input v-model="verifyForm.address" placeholder="请输入居住地址" maxlength="200" />
                </el-form-item>
                <el-form-item label="职业" prop="occupation">
                  <el-input v-model="verifyForm.occupation" placeholder="请输入职业" maxlength="50" />
                </el-form-item>
                <template v-if="userStore.isAdopter">
                  <el-form-item label="养宠经验" prop="petExperience">
                    <el-input v-model="verifyForm.petExperience" type="textarea" :rows="2" placeholder="请描述您的养宠经验" maxlength="500" show-word-limit />
                  </el-form-item>
                  <el-form-item label="居住环境" prop="livingEnvironment">
                    <el-input v-model="verifyForm.livingEnvironment" type="textarea" :rows="2" placeholder="请描述您的居住环境" maxlength="500" show-word-limit />
                  </el-form-item>
                </template>
                <template v-if="userStore.isRescuer">
                  <el-form-item label="机构名称" prop="orgName">
                    <el-input v-model="verifyForm.orgName" placeholder="请输入机构名称" maxlength="100" />
                  </el-form-item>
                  <el-form-item label="机构资质">
                    <el-upload
                      :action="'/api/file/upload'"
                      :headers="{ Authorization: `Bearer ${userStore.token}` }"
                      :on-success="(res) => verifyForm.orgLicense = res.data"
                      :show-file-list="false"
                    >
                      <el-button>上传资质文件</el-button>
                    </el-upload>
                    <span v-if="verifyForm.orgLicense" style="margin-left: 12px; color: #52C41A">已上传</span>
                  </el-form-item>
                </template>
                <el-form-item>
                  <el-button type="primary" :loading="verifyLoading" @click="handleSubmitVerify">提交认证</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { updateUser, submitVerify } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const activeTab = ref('basic')
const saveLoading = ref(false)
const verifyLoading = ref(false)

const basicForm = reactive({
  realName: '',
  phone: '',
  email: '',
  address: ''
})

const verifyForm = reactive({
  realName: '',
  idCard: '',
  address: '',
  occupation: '',
  petExperience: '',
  livingEnvironment: '',
  orgName: '',
  orgLicense: ''
})

// 身份证号校验
const validateIdCard = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const verifyRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度2-20个字符', trigger: 'blur' }
  ],
  idCard: [
    { required: true, validator: validateIdCard, trigger: 'blur' }
  ]
}

const initForms = () => {
  const info = userStore.userInfo || {}
  Object.assign(basicForm, {
    realName: info.realName || '',
    phone: info.phone || '',
    email: info.email || '',
    address: info.address || ''
  })
  Object.assign(verifyForm, {
    realName: info.realName || '',
    idCard: info.idCard || '',
    address: info.address || '',
    occupation: info.occupation || '',
    petExperience: info.petExperience || '',
    livingEnvironment: info.livingEnvironment || '',
    orgName: info.orgName || '',
    orgLicense: info.orgLicense || ''
  })
}

const handleSaveBasic = async () => {
  saveLoading.value = true
  try {
    await updateUser(basicForm)
    userStore.updateUserInfo(basicForm)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败，请重试')
  } finally {
    saveLoading.value = false
  }
}

const handleSubmitVerify = async () => {
  await verifyFormRef.value.validate()
  verifyLoading.value = true
  try {
    await submitVerify(verifyForm)
    await userStore.fetchUserInfo()
    ElMessage.success('认证提交成功，请等待审核')
  } catch (error) {
    ElMessage.error(error.message || '认证提交失败，请重试')
  } finally {
    verifyLoading.value = false
  }
}

onMounted(() => {
  initForms()
})
</script>

<style lang="scss" scoped>
.profile-card {
  text-align: center;
  padding: 32px;
  background: #f5f7fa;
  border-radius: 12px;
  
  h3 {
    margin: 16px 0 12px;
    font-size: 20px;
    color: #303133;
  }
}
</style>
