<template>
  <div>
    <h1>IP地址维护</h1>

    <el-row class="ip-content-box">
      <el-alert
        class="time-tip"
        v-if="timeCountShow"
        :title=timeCountStr
        type="success"
        :closable=false
        center>
      </el-alert>
      <el-col :span="6">
        <el-progress v-if="IPProgressShow" :text-inside="true" :stroke-width="18" :percentage="IPProgress" :status="IPProgressStatus" class="progress-box"></el-progress>
        <el-container>
          <el-main>
            <el-upload
              ref="importIPFile"
              class="ip-file-upload"
              accept=".csv"
              name="importIPFileInput"
              drag
              :action=upload_url
              :limit=1
              :multiple="false"
              :auto-upload="false"
              :before-upload="importIPFile"
              :on-success="importIPFileFinish"
              :on-error="importIPFileFinish">

              <i class="el-icon-upload"></i>
              <div class="el-upload__text"><em>IP地址文件</em></div>
              <div class="el-upload__tip" slot="tip">请导入csv文件</div>
            </el-upload>
          </el-main>
        </el-container>
      </el-col>

      <el-col :span="6">
        <el-progress v-if="cityProgressShow" :text-inside="true" :stroke-width="18" :percentage="cityProgress" :status="cityProgressStatus" class="progress-box"></el-progress>
        <el-container>
          <el-main>
            <el-upload
              ref="importCityFile"
              class="ip-file-upload"
              accept=".csv"
              name="importCityFileInput"
              drag
              :action=upload_url
              :limit=1
              :multiple="false"
              :auto-upload="false"
              :before-upload="importCityFile"
              :on-success="importCityFileFinish"
              :on-error="importCityFileFinish">
              <i class="el-icon-upload"></i>
              <div class="el-upload__text"><em>城市代码文件</em></div>
              <div class="el-upload__tip" slot="tip">请导入csv文件</div>
            </el-upload>
          </el-main>
        </el-container>
      </el-col>

      <el-col :span="6">
        <el-progress v-if="cardProgressShow" :text-inside="true" :stroke-width="18" :percentage="cardProgress" :status="cardProgressStatus" class="progress-box"></el-progress>
        <el-container>
          <el-main>
            <el-upload
              ref="importCardFile"
              class="ip-file-upload"
              accept=".csv"
              name="importCardFileInput"
              drag
              :action=upload_url
              :limit=1
              :multiple="false"
              :auto-upload="false"
              :before-upload="importCardFile"
              :on-success="importCardFileFinish"
              :on-error="importCardFileFinish">

              <i class="el-icon-upload"></i>
              <div class="el-upload__text"><em>身份证号段文件</em></div>
              <div class="el-upload__tip" slot="tip">请导入csv文件</div>
            </el-upload>
          </el-main>
        </el-container>
      </el-col>

      <el-col :span="6">
        <el-progress v-if="mobileProgressShow" :text-inside="true" :stroke-width="18" :percentage="mobileProgress" :status="mobileProgressStatus" class="progress-box"></el-progress>
        <el-container>
          <el-main>
            <el-upload
              ref="importMobileFile"
              class="ip-file-upload"
              accept=".csv"
              name="importMobileFileInput"
              drag
              :action=upload_url
              :limit=1
              :multiple="false"
              :auto-upload="false"
              :before-upload="importMobileFile"
              :on-success="importMobileFileFinish"
              :on-error="importMobileFileFinish">
              <i class="el-icon-upload"></i>
              <div class="el-upload__text"><em>手机号段文件</em></div>
              <div class="el-upload__tip" slot="tip">请导入csv文件</div>
            </el-upload>
          </el-main>
        </el-container>
      </el-col>
    </el-row>

    <div style="margin-top:50px;">
      <el-button class="submit-btn" type="success" @click="submitForm()">确定</el-button>
    </div>
    <el-row>

    </el-row>

  </div>
</template>

<script>
  import ajax from '@/common/ajax'
  import util from '@/common/util'

  let getProgressTimer = null

  export default {
    computed: {
      timeCountShow: function () {
        return this.timeCount !== 0
      },
      timeCountStr: function () {
        let hour = 0
        let minute = 0
        let second = 0
        hour = parseInt(this.timeCount / 60 / 60)
        if (hour < 10) {
          hour = '0' + hour
        }
        minute = parseInt(this.timeCount / 60 % 60)
        if (minute < 10) {
          minute = '0' + minute
        }
        second = parseInt(this.timeCount % 60)
        if (second < 10) {
          second = '0' + second
        }
        return hour + ':' + minute + ':' + second
      },
      IPProgressStatus () {
        return this.IPProgress === 100 ? 'success' : ''
      },
      cityProgressStatus () {
        return this.cityProgress === 100 ? 'success' : ''
      },
      cardProgressStatus () {
        return this.cardProgress === 100 ? 'success' : ''
      },
      mobileProgressStatus () {
        return this.mobileProgress === 100 ? 'success' : ''
      }
    },
    data () {
      return {
        timeCount: 0,
        IPProgressShow: false,
        cityProgressShow: false,
        cardProgressShow: false,
        mobileProgressShow: false,
        IPProgress: 0,
        cityProgress: 0,
        cardProgress: 0,
        mobileProgress: 0,
        upload_url: 'higitech', // 随便填一个，但一定要有
        uploadForm: new FormData(),   // 一个formdata
        rules: {}     // 用到的规则
      }
    },
    methods: {
      submitForm () {
        this.uploadForm = new FormData()

        let importIPFileExist = false
        let importCityFileExist = false
        let importCardFileExist = false
        let importMobileFileExist = false
        let importIPFileName = ''
        let importCityFileName = ''
        let importCardFileName = ''
        let importMobileFileName = ''

        try {
          let importIPFileFiles = this.$refs.importIPFile.$data.uploadFiles
          let importCityFileFiles = this.$refs.importCityFile.$data.uploadFiles
          let importCardFileFiles = this.$refs.importCardFile.$data.uploadFiles
          let importMobileFileFiles = this.$refs.importMobileFile.$data.uploadFiles

          if (importIPFileFiles && importIPFileFiles[0]) {
            importIPFileExist = true
            importIPFileName = importIPFileFiles[0].name
          }
          if (importCityFileFiles && importCityFileFiles[0]) {
            importCityFileExist = true
            importCityFileName = importCityFileFiles[0].name
          }
          if (importCardFileFiles && importCardFileFiles[0]) {
            importCardFileExist = true
            importCardFileName = importCardFileFiles[0].name
          }
          if (importMobileFileFiles && importMobileFileFiles[0]) {
            importMobileFileExist = true
            importMobileFileName = importMobileFileFiles[0].name
          }
        } catch (e) {
          console.log(e)
        }

        if (!importIPFileExist) {
          this.$message('请选择IP地址文件')
          return
        }
        if (!importCityFileExist) {
          this.$message('请选择城市代码文件')
          return
        }
        if (!importIPFileName.endsWith('.csv')) {
          this.$message('IP地址文件必须为csv文件')
          return
        }
        if (!importCityFileName.endsWith('.csv')) {
          this.$message('城市代码文件必须为csv文件')
          return
        }
        if (importCardFileExist && !importCardFileName.endsWith('.csv')) {
          this.$message('身份证号段文件必须为csv文件')
          return
        }
        if (importMobileFileExist && !importMobileFileName.endsWith('.csv')) {
          this.$message('手机号段文件必须为csv文件')
          return
        }
        this.$refs.importIPFile.submit()
        this.$refs.importCityFile.submit()   // 提交时触发了before-upload函数
        this.$refs.importCardFile.submit()
        this.$refs.importMobileFile.submit()

        let self = this
        this.progressReset()
        this.timeCount = 0
        try {
          window.clearInterval(getProgressTimer)
        } catch (e) {}

        if (importIPFileExist) {
          this.IPProgressShow = true
        }
        if (importCityFileExist) {
          this.cityProgressShow = true
        }
        if (importCardFileExist) {
          this.cardProgressShow = true
        }
        if (importMobileFileExist) {
          this.mobileProgressShow = true
        }
        ajax.post({
          url: '/tms/ip/import',
          param: self.uploadForm,
          success: function (data) {
            self.uploadForm = new FormData()
            getProgressTimer = window.setInterval(self.getImportProgress, 1000)
          },
          error: function (data) {
            self.uploadForm = new FormData()
            alert(data.error)
          },
          fail: function (err) {
            self.uploadForm = new FormData()
            alert('导入文件发生错误')
            console.log(err)
          }
        })
      },
      getImportProgress () {
        let self = this
        ajax.post({
          url: '/tms/ip/getProgress',
          param: {},
          success: function (data) {
            let errorInfo = data.errorinfo
            let IPProgress = data.ipprogress
            let cityProgress = data.cityprogress
            let cardProgress = data.cardprogress
            let mobileProgress = data.mobileprogress
            console.log(`${IPProgress}-${cityProgress}-${cardProgress}-${mobileProgress}`)

            self.IPProgress = IPProgress
            self.cityProgress = cityProgress
            self.cardProgress = cardProgress
            self.mobileProgress = mobileProgress
            if (!util.isEmpty(errorInfo)) {
              alert(errorInfo)
              console.log('clear timer')
              window.clearInterval(getProgressTimer)
            }
            self.timeCount++

            if (!(util.isEmpty(errorInfo) && (IPProgress < 100 || cityProgress < 100 ||
                cardProgress < 100 || mobileProgress < 100))) {
              try {
                console.log('clear timer')
                window.clearInterval(getProgressTimer)
              } catch (e) {
                console.log(e)
              }
            }
          }
        })
      },
      progressReset () {
        this.IPProgress = 0
        this.cityProgress = 0
        this.cardProgress = 0
        this.mobileProgress = 0
        this.IPProgressShow = false
        this.cityProgressShow = false
        this.cardProgressShow = false
        this.mobileProgressShow = false
      },
      importIPFile (file) {
        console.log(123)
        this.uploadForm.append('importIPFile', file)
        this.$refs.importIPFile.abort()
        return false
      },
      importCityFile (file) {
        this.uploadForm.append('importCityFile', file)
        this.$refs.importIPFile.abort()
        return false
      },
      importCardFile (file) {
        this.uploadForm.append('importCardFile', file)
        this.$refs.importIPFile.abort()
        return false
      },
      importMobileFile (file) {
        this.uploadForm.append('importMobileFile', file)
        this.$refs.importIPFile.abort()
        return false
      },
      importIPFileFinish () {
        return false
        // this.$refs.importIPFile.clearFiles()
      },
      importCityFileFinish () {
        return false
        // this.$refs.importCityFile.clearFiles()
      },
      importCardFileFinish () {
        return false
        // this.$refs.importCardFile.clearFiles()
      },
      importMobileFileFinish () {
        return false
        // this.$refs.importMobileFile.clearFiles()
      }
    }
  }
</script>

<style>
  .el-upload-dragger{
    width: 180px;
  }
  .submit-btn{
    width: 200px;
  }
  .progress-box{
    width: 230px;
    margin: auto;
  }
  .time-tip{
    margin-bottom: 20px;
  }
  .ip-content-box{
    margin-top: 10vh;
  }
</style>
