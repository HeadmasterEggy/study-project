<template>
    <div>
        <div style="margin: 30px 20px">
            <el-steps :active="active" align-center finish-status="success">
                <el-step title="验证电子邮件"/>
                <el-step title="重新设定密码"/>
            </el-steps>
        </div>
        <transition mode="out-in" name="el-fade-in-linear">
            <div v-if="active === 0" style="text-align: center;margin: 0 20px;height: 100%">
                <div style="margin-top: 80px">
                    <div style="font-size: 25px;font-weight: bold">重置密码</div>
                    <div style="font-size: 14px;color: grey">请输入需要重置密码的电子邮件地址</div>
                </div>
                <div style="margin-top: 50px">
                    <el-form ref="formRef" :model="form" :rules="rules" @validate="onValidate">
                        <el-form-item prop="email">
                            <el-input v-model="form.email" placeholder="电子邮件地址" type="email">
                                <template #prefix>
                                    <el-icon>
                                        <Message/>
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="code">
                            <el-row :gutter="10" style="width: 100%">
                                <el-col :span="17">
                                    <el-input v-model="form.code" :maxlength="6" placeholder="请输入验证码" type="text">
                                        <template #prefix>
                                            <el-icon>
                                                <EditPen/>
                                            </el-icon>
                                        </template>
                                    </el-input>
                                </el-col>
                                <el-col :span="5">
                                    <el-button :disabled="!isEmailValid || coldTime > 0" type="success"
                                               @click="validateEmail">
                                        {{ coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码' }}
                                    </el-button>
                                </el-col>
                            </el-row>
                        </el-form-item>
                    </el-form>
                </div>
                <div style="margin-top: 70px">
                    <el-button plain style="width: 270px;" type="danger" @click="startReset()">重置密码</el-button>
                </div>
            </div>
        </transition>
        <transition mode="out-in" name="el-fade-in-linear">
            <div v-if="active === 1" style="text-align: center;margin: 0 20px;height: 100%">
                <div style="margin-top: 80px">
                    <div style="font-size: 25px;font-weight: bold">重置密码</div>
                    <div style="font-size: 14px;color: grey">请填写您的新密码，务必牢记，防止丢失</div>
                </div>
                <div style="margin-top: 50px">
                    <el-form ref="formRef" :model="form" :rules="rules" @validate="onValidate">
                        <el-form-item prop="password">
                            <el-input v-model="form.password" :maxlength="16" placeholder="新密码" type="password">
                                <template #prefix>
                                    <el-icon>
                                        <Lock/>
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="password_repeat">
                            <el-input v-model="form.password_repeat" :maxlength="16" placeholder="重复新密码"
                                      type="password">
                                <template #prefix>
                                    <el-icon>
                                        <Lock/>
                                    </el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-form>
                </div>
                <div style="margin-top: 70px">
                    <el-button plain style="width: 270px;" type="danger" @click="doReset()">立即重置密码</el-button>
                </div>
            </div>
        </transition>
    </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";

const active = ref(0)

const form = reactive({
    email: '',
    code: '',
    password: '',
    password_repeat: '',
})

const validatePassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
        callback(new Error("两次输入的密码不一致"))
    } else {
        callback()
    }
}

const rules = {
    email: [
        {required: true, message: '请输入邮件地址', trigger: 'blur'},
        {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
    ],
    code: [
        {required: true, message: '请输入获取的验证码', trigger: 'blur'},
    ],
    password: [
        {required: true, message: '请输入密码', trigger: 'blur'},
        {min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change']}
    ],
    password_repeat: [
        {validator: validatePassword, trigger: ['blur', 'change']},
    ],
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
    if (prop === 'email')
        isEmailValid.value = isValid
}

const validateEmail = () => {
    coldTime.value = 60
    post('/api/auth/valid-reset-email', {
        email: form.email
    }, (message) => {
        ElMessage.success(message)
        setInterval(() => coldTime.value--, 1000)
    }, (message) => {
        ElMessage.warning(message)
        coldTime.value = 0
    })
}

const startReset = () => {
    formRef.value.validate((isValid) => {
        if (isValid) {
            post('/api/auth/start-reset', {
                email: form.email,
                code: form.code
            }, () => {
                active.value++
            })
        } else {
            ElMessage.warning('请填写电子邮件地址和验证码')
        }
    })
}

const doReset = () => {
    formRef.value.validate((isValid) => {
        if (isValid) {
            post('/api/auth/do-reset', {
                password: form.password
            }, (message) => {
                ElMessage.success(message)
                router.push('/')
            })
        } else {
            ElMessage.warning('请填写新的密码')
        }
    })
}

</script>

<style scoped>

</style>
