import Vue from 'vue'
import BookList from '@/components/BookList'

const url = 'http://localhost:9876/'

/**
 * 正常测试用例
 * 前提条件：构建一个book-list组件，设置组件的heading和books属性，绑定onBookSelect事件
 * 测试：1、heading绑定值是否正确
 *      2、class 为book的长度是否等于books数组的长度
 *      3、验证book.img_url，book.title，book.authors | join
 *      4、测试join函数
 *      5、触发onBookSelect点击事件，传入的book是否正确
 *
 **/
describe('book-list', () => {
  const promotions = [
    {
      "id": 1,
      "title": "揭开数据真相：从小白到数据分析达人",
      "authors": [
        "Edward Zaccaro, Daniel Zaccaro"
      ],
      "img_url": "1.svg"
    },
    {
      "id": 2,
      "title": "Android 高级进阶",
      "authors": [
        "顾浩鑫"
      ],
      "img_url": "2.svg"
    }
  ]
  var vm = undefined

  function join (args) {
    return args.join(',')
  }

  before(() => {
    const HtmlContainer = Vue.extend({
      components: {
        BookList
      },
      data () {
        return {
          promotions: promotions
        }
      },
      template: `<book-list heading="最新更新" :books="promotions" @onBookSelect="preview($event)"></book-list>`,
      methods: {
        preview (book) {
          alert("显示图书详情")
        }
      }
    })
    vm = new HtmlContainer().$mount()
  })

  // afterEach(() => {
  //   console.log('afterEach')
  //   console.log(vm.destroy)
  //   vm.destroy()
  // })

  it('#heading', () => {
    expect(vm.$el.querySelector('.header .heading').textContent)
      .to.equal('最新更新')
  })

  it('#books', () => {
    var books = vm.$el.querySelectorAll('.book')
    var book = books[0]

    expect(books.length).to.equal(2)

    expect(book.querySelector('.cover img').src)
      .to.equal(url + promotions[0].img_url)
    expect(book.querySelector('.title').textContent)
      .to.equal(promotions[0].title)
    expect(book.querySelector('.authors').textContent)
      .to.equal(join(promotions[0].authors))
  })

  // 无法获取到过滤器函数
  xit('#join', () => {

  })

  xit('#onBookSelect', () => {

  })

  it('待定的测试')
})


/**
 * 异常测试用例
 * 前提条件：构建一个book-list组件，不设置组件的heading和books属性，不绑定onBookSelect事件
 * 测试：1、heading绑定值是否正确
 *      2、class 为book的长度是否为0
 *      3、验证book.img_url，book.title，book.authors | join
 *      4、测试join函数
 *      5、触发onBookSelect点击事件
 *
 **/

/**
 * 异常测试用例
 * 前提条件：构建一个book-list组件，设置组件的heading为‘’，books为[]，不绑定onBookSelect事件
 * 测试：1、heading绑定值是否正确
 *      2、class 为book的长度是否为0
 *      3、验证book.img_url，book.title，book.authors | join
 *      4、测试join函数
 *      5、触发onBookSelect点击事件
 *
 **/
