var googleCommands = {
  submit: function() {
    this.api.pause(1000);
    return this.waitForElementVisible('@submitButton', 1000)
      .click('@submitButton')
      .waitForElementNotPresent('@listDialogform');
  }
};

module.exports = {
  // commands: [googleCommands],
  url: function() {
    return 'http://localhost:8080/list';
    // return this.api.launchUrl + '/list';
  },
  sections: {
    list: {
      selector: '.el-main',
      sections: {
        listForm: {
          selector: '.el-form',
          elements: {
            listForm: {
              selector: '.el-form'
            }
          }
        },
        listDialog: {
          selector: '.el-dialog',
          sections: {
            listDialogform: {
              selector: '.el-dialog__body .el-form',
              elements: {
                rostername: {
                  selector: '[data=rostername] input'
                },
                rosternameError: {
                  selector: '[data=rostername] .el-form-item__error'
                },
                rosterdesc: {
                  selector: '[data=rosterdesc] input'
                },
                rosterdescError: {
                  selector: '[data=rosterdesc] .el-form-item__error'
                },
                datatype: {
                  selector: '[data=datatype] input'
                },
                datatypeError: {
                  selector: '[data=datatype] .el-form-item__error'
                },
                rostertype: {
                  selector: '[data=rostertype] input'
                },
                rostertypeError: {
                  selector: '[data=rostertype] .el-form-item__error'
                },
                iscache: {
                  selector: '[data=iscache] input'
                },
                remark: {
                  selector: '[data=remark] textarea'
                },
                remarkError: {
                  selector: '[data=remark] .el-form-item__error'
                }
              }
            }
          },
          elements: {
            title: {
              selector: '.el-dialog__title'
            },
            cancelBtn: {
              selector: '.dialog-footer [data=cancelBtn]'
            },
            saveBtn: {
              selector: '.dialog-footer [data=saveBtn]'
            }
          }
        }
      },
      elements: {
        selBtn: {
          selector: '#selBtn'
        },
        addBtn: {
          selector: '#addBtn'
        }
      }
    }
  }
};
