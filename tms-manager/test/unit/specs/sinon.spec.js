describe('sinon', () => {
  it('spies', () => {
    let user = {
      setName (name) {
        this.name = name
      }
    }

    const setNameSpy = sinon.spy(user, 'setName');

    user.setName("name1")

    console.log(setNameSpy.callCount);

    user.setName("name2")
    expect(setNameSpy.callCount).to.equal(2)

    setNameSpy.restore()
  })

  it('spies2', () => {
    const myFunction = (condition, callback) => {
      if (condition) {
        callback()
      }
    }

    let callback = sinon.spy()
    myFunction("condition", callback)

    // expect 的语法则是用 expect(value)取代spy.should
    callback.should.have.been.calledOnce
    expect(callback).have.been.calledOnce
  })
})
