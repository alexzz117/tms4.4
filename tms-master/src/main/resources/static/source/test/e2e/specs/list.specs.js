/**
 * Created by Administrator on 2017/12/14.
 */
module.exports = {
  'listPageTest': function (client) {
    var list = client.page.list();
    var listSection = list.section.list
    var listDialog = listSection.section.listDialog
    var listDialogform = listDialog.section.listDialogform;
    var listDialogformData;

    var errorInfo = {
      checkFormEnSpecialCharacter: '请以字母开头，包括字母、数字和下划线',
      checkFormSpecialCode: '请不要输入特殊字符！如($,%)'
    }

    // 检查listDialogform
    function checklistDialogform(formSection, formData) {
      formSection.setValue('@rostername', formData.rostername)
      formSection.setValue('@rosterdesc', formData.rosterdesc)
      formSection.setValue('@remark', formData.remark)
    }

    list.navigate()
      .waitForElementVisible('#app', 10000)

    function addInit() {
      // 点击新建按钮
      listSection.click('@addBtn')

      // 打开对话框
      listSection.expect.section('@listDialog').to.be.visible;

      // 查看对话框title
      listDialog.expect.element('@title').text.to.equal('新建名单');

    }

    function clickCancelBtn() {
      // 点击取消按钮，注意，此处记得设置暂停时间
      listDialog.click('@cancelBtn');
      client.pause(1000);

      // 关闭对话框
      listSection.expect.section('@listDialog').to.not.be.visible;
    }

    /**
     ***************************新增名单**********************
     */
    //*****************异常测试****************************
    // 全部输入为空
    addInit()
    listDialogformData = {
      rostername: "",
      rosterdesc: "",
      datatype: "",
      rostertype: "",
      iscache: "",
      remark: "",
    }
    checklistDialogform(listDialogform, listDialogformData);
    client.pause(1000);
    listDialog.click('@saveBtn');

    // 核对表单校验信息
    listDialogform.expect.element('@rosternameError').to.be.visible;
    listDialogform.expect.element('@rosternameError').text.to.equal('请输入名单英文名');
    listDialogform.expect.element('@rosterdescError').to.be.visible;
    listDialogform.expect.element('@rosterdescError').text.to.equal('请输入名单名称');
    listDialogform.expect.element('@datatypeError').to.be.visible;
    listDialogform.expect.element('@datatypeError').text.to.equal('请输入名单数据类型');
    listDialogform.expect.element('@rostertypeError').to.be.visible;
    listDialogform.expect.element('@rostertypeError').text.to.equal('请输入名单类型');
    client.pause(1000);
    clickCancelBtn();

    // 异常特殊字符测试
    addInit()
    listDialogformData = {
      rostername: "@!$#@%#$^%",
      rosterdesc: "@!$#@%#$^%",
      datatype: "1",
      rostertype: "1",
      iscache: "1",
      remark: "@!$#@%#$^%"
    }

    checklistDialogform(listDialogform, listDialogformData);
    client.pause(1000);

    // 点击保存
    listDialog.click('@saveBtn');

    // 核对表单校验信息
    listDialogform.expect.element('@rosternameError').to.be.visible;
    listDialogform.expect.element('@rosternameError').text.to.equal(errorInfo.checkFormEnSpecialCharacter);
    listDialogform.expect.element('@rosterdescError').to.be.visible;
    listDialogform.expect.element('@rosterdescError').text.to.equal(errorInfo.checkFormSpecialCode);
    listDialogform.expect.element('@remarkError').to.be.visible;
    listDialogform.expect.element('@remarkError').text.to.equal(errorInfo.checkFormSpecialCode);
    client.pause(1000);
    clickCancelBtn()

    // 异常边界测试
    addInit()
    listDialogformData = {
      rostername: "33个字符abcdefghijklmeopkddddddddddd",
      rosterdesc: "65个字符abcdefghijklmeopkddddddddddddddddddddddddddddddddddddddddddd",
      datatype: "1",
      rostertype: "1",
      iscache: "1",
      remark: "513个字符ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
    }

    checklistDialogform(listDialogform, listDialogformData);
    client.pause(1000);

    // 点击保存
    listDialog.click('@saveBtn');

    // 核对表单校验信息
    listDialogform.expect.element('@rosternameError').to.be.visible;
    listDialogform.expect.element('@rosternameError').text.to.equal('长度在32个字符以内');
    listDialogform.expect.element('@rosterdescError').to.be.visible;
    listDialogform.expect.element('@rosterdescError').text.to.equal('长度在64个字符以内');
    listDialogform.expect.element('@remarkError').to.be.visible;
    listDialogform.expect.element('@remarkError').text.to.equal('长度在512个字符以内');
    client.pause(1000);
    clickCancelBtn()

    // 异常字符长度测试
    addInit()
    listDialogformData = {
      rostername: "68个字符abcdefghijklmeopkdddddddddddddddddddddddddddddddddddddddddddddd",
      rosterdesc: "68个字符abcdefghijklmeopkdddddddddddddddddddddddddddddddddddddddddddddd",
      datatype: "1",
      rostertype: "1",
      iscache: "1",
      remark: "591个字符ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
    }

    checklistDialogform(listDialogform, listDialogformData);
    client.pause(1000);

    // 点击保存
    listDialog.click('@saveBtn');

    // 核对表单校验信息
    listDialogform.expect.element('@rosternameError').to.be.visible;
    listDialogform.expect.element('@rosternameError').text.to.equal('长度在32个字符以内');
    listDialogform.expect.element('@rosterdescError').to.be.visible;
    listDialogform.expect.element('@rosterdescError').text.to.equal('长度在64个字符以内');
    listDialogform.expect.element('@remarkError').to.be.visible;
    listDialogform.expect.element('@remarkError').text.to.equal('长度在512个字符以内');
    client.pause(1000);
    clickCancelBtn()

    //*****************正常测试****************************

    // 正常边界测试
    addInit()
    listDialogformData = {
      rostername: "d_32_abcdefghijklmeopkdddddddddd",
      rosterdesc: "64个字符abcdefghijklmeopkdddddddddddddddddddddddddddddddddddddddddd",
      datatype: "1",
      rostertype: "1",
      iscache: "1",
      remark: "512个字符dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
    }

    checklistDialogform(listDialogform, listDialogformData);
    client.pause(1000);

    // 点击保存
    listDialog.click('@saveBtn');

    // 核对表单校验信息
    listDialogform.expect.element('@rosternameError').to.not.be.present;
    listDialogform.expect.element('@rosterdescError').to.not.be.present;
    listDialogform.expect.element('@remarkError').to.not.be.present;

    client.pause(1000);

    client.end();
  }
};
