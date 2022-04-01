console.log("plugincontent:start");


let JSBridge = {
  postMessage: function (message) {
    console.log("plugincontent:postMessage: " + JSON.stringify(JSBridge))
    browser.runtime.sendMessage({
      action: "JSBridge",
      data: message
    });
  }
}
window.wrappedJSObject.JSBridge = cloneInto(
  JSBridge,
  window,
  { cloneFunctions: true });

console.log("plugincontent:JSBRIDGE: " + JSON.stringify(JSBridge))

browser.runtime.onMessage.addListener((data, sender) => {
  //console.log("plugincontent:eval:" + data);
  if (data.action === 'evalJavascript') {
    let evalCallBack = {
      id: data.id,
      action: "evalJavascript",
    }
    try {
      let result = window.eval(data.data);
      console.log("plugincontent:eval:result" + result);
      if (result) {
        evalCallBack.data = result;
      } else {
        evalCallBack.data = "";
      }
    } catch (e) {
      evalCallBack.data = e.toString();
      return Promise.resolve(evalCallBack);
    }
    return Promise.resolve(evalCallBack);
  }
});
console.log("plugincontent:end");
