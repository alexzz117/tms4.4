/**
 * Created by Administrator on 2017/12/14.
 */
module.exports = {
  url: 'https://www.baidu.com/',
  elements: {
    searchBar: {
      selector: '#kw'
    },
    submit: {
      selector: '//[@name="q"]',
      locateStrategy: 'xpath'
    }
  }
};
